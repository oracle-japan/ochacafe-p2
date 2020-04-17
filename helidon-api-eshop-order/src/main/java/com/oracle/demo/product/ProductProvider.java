package com.oracle.demo.product;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.oracle.demo.product.entity.InventoryEntity;
import com.oracle.demo.product.entity.ProductEntity;
import com.oracle.demo.product.entity.SearchResultEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;

/**
 * 商品検索・情報取得のビジネスロジックを集約したプロバイダ・クラス
 */
@Dependent
public class ProductProvider {
	@PersistenceContext(unitName = "ProductUnit")
	private EntityManager em;

	private CDITraceScope tracer;

	@Inject
	protected ProductProvider(CDITraceScope tracer) {
		this.tracer = tracer;
	}

	/**
	 * 商品一覧の取得
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param descending 降順ソートの場合にtrue
	 * @return 商品一覧のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public SearchResultEntity search(int offset, int limit, boolean descending) {
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
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param order 価格のソート順を昇順("asc") or 降順("desc")で指定。指定が無い場合、無効な文字の場合は価格の昇順でソート。
	 */
	/**
	 * 商品情報を取得する
	 * @param productId 商品ID
	 * @return 商品情報のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public ProductEntity getProduct(String productId) {
		try {
			Scope scope = tracer.begin("get_product");

			ProductEntity product = em.find(ProductEntity.class,  productId);

			if(product == null) {
				String errorMessage =String.format("Resource for product-id \"%s\" not found", productId);
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
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public Collection<InventoryEntity> retrieveStockCounts(Collection<String> productList) {
		try {
			ArrayList<InventoryEntity> result = new ArrayList<InventoryEntity>();

			Scope scope = tracer.begin("checkout_order");

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
