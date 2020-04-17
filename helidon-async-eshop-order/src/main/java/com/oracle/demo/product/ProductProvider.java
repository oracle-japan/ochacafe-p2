package com.oracle.demo.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.oracle.demo.product.entity.InventoryEntity;
import com.oracle.demo.product.entity.ProductEntity;
import com.oracle.demo.product.entity.SearchResultEntity;

import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.TraceInjector;
import com.oracle.demo.util.observable.Traceable;
import com.oracle.demo.util.streaming.KafkaProvider;

import io.opentracing.Scope;

@Dependent
public class ProductProvider {
	@PersistenceContext(unitName = "ProductUnit")
	private EntityManager em;

	@Inject
	@ConfigProperty(name = "app.product.stream.name")
	private String streamName;

	private CDITraceScope tracer;
	private KafkaProvider kafkaProvider;

	@Inject
	protected ProductProvider(CDITraceScope tracer, KafkaProvider kafkaProvider) {
		this.kafkaProvider = kafkaProvider;
		this.tracer = tracer;
	}

	private JsonArray toProductListJson(ProductEntity[] products) {
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(ProductEntity product : products) {
			jab.add(toProductJson(product));
		}

		return jab.build();
	}

	private JsonObject toProductJson(ProductEntity product) {
		return Json.createObjectBuilder()
				.add("id", product.getId())
				.add("name", product.getName())
				.add("variation", product.getVariation())
				.add("category_id", product.getCategoryId())
				.add("category_name", product.getCategoryName())
				.add("price", product.getPrice())
				.add("supplier_id", product.getSupplierId())
				.add("supplier_name", product.getSupplierName())
				.add("stock_count", product.getStockCount())
				.build();
	}

	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public void updateProduct(ProductEntity product) {

		JsonObject json = toProductJson(product);

		Scope scope = tracer.begin("update_products");

		if (em.find(ProductEntity.class, product.getId()) != null) {
			em.merge(product);
		} else {
			em.persist(product);
		}

		//TODO:  外部のTransactionコンテキストによるロールバックは考慮しない
//		em.flush();

		// ここからKafkaにポスト
		String messageKey = "product-update:" + UUID.randomUUID().toString();
		Producer<String, String> producer = kafkaProvider.producer();
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(streamName, messageKey, json.toString());

		// Kafka ヘッダにトレース情報をinject
		tracer.inject(TraceInjector.KafkaProducerRecordCarrier.build(record));

		producer.send(record);
		producer.close();

		tracer.end(scope);
	}

	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public void updateProducts(ProductEntity[] products) {

		JsonArray json = toProductListJson(products);

		for (ProductEntity product : products) {
			if (em.find(ProductEntity.class, product.getId()) != null) {
				em.merge(product);
			} else {
				em.persist(product);
			}
		}

		//TODO:  外部のTransactionコンテキストによるロールバックは考慮しない
//		em.flush();

		// ここからKafkaにポスト
		String messageKey = "product-update:" + UUID.randomUUID().toString();
		Producer<String, String> producer = kafkaProvider.producer();
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(streamName, messageKey, json.toString());

		// Kafka ヘッダにトレース情報をinject
		tracer.inject(TraceInjector.KafkaProducerRecordCarrier.build(record));

		producer.send(record);
		producer.close();
	}

	/**
	 * 商品の一覧を取得する
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param descending 降順ソートの場合にtrue
	 * @return 商品一覧を示すエンティティ
	 */
	public SearchResultEntity searchProducts(int offset, int limit, boolean descending) {
		Collection<ProductEntity> results = null;

		try {
			String namedQuery = (descending ? "ProductEntity.All.Descending" : "ProductEntity.All.Ascending");

			Scope scope = tracer.begin("search_products");

			TypedQuery<ProductEntity> query = em.createNamedQuery(namedQuery, ProductEntity.class)
					.setFirstResult(offset);
			results = (limit > 0 ? query.setMaxResults(limit) : query).getResultList();

			tracer.end(scope);

			return new SearchResultEntity(results, offset);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 商品情報を取得する
	 * @param productId 商品ID
	 * @return 商品情報のエンティティ
	 */
	public ProductEntity getProduct(String productId) {
		try {
			Scope scope = tracer.begin("get_product");

			ProductEntity product = em.find(ProductEntity.class,  productId);

			if(product == null) {
				String errorMessage = String.format("Resource for product-id \"%s\" not found", productId);
				tracer.end(scope, errorMessage);
				return null;
			}

			tracer.end(scope);

			return product;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 商品IDのリストから在庫数のリストを取得する。無効な商品IDの場合は 0 を返却する。
	 * @param productList 取得する商品IDのリスト。
	 * @return 各商品IDに対応する在庫数のリスト。
	 */
	public Collection<InventoryEntity> retrieveStockCounts(Collection<String> productList) {
		try {
			ArrayList<InventoryEntity> result = new ArrayList<InventoryEntity>();

			Scope scope = tracer.begin("get_inventory");

			for (String productId : productList) {
				InventoryEntity inventory = em.find(InventoryEntity.class, productId);
				result.add(inventory != null ? inventory : InventoryEntity.emptyInventory(productId));
			}

			tracer.end(scope);

			return result;
		} catch (Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
	}

}
