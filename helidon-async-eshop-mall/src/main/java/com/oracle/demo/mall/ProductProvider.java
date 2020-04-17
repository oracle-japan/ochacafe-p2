package com.oracle.demo.mall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.oracle.demo.mall.entity.ProductEntity;
import com.oracle.demo.mall.entity.SearchResultEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;

@Dependent
public class ProductProvider {
	private static final String PRODUCT_PREFIX = "product:";
	private static final String PRICE_SORT_LIST = "product-sort:price";

	private CDITraceScope tracer;
	private JedisPool pool;

	@Inject
	protected ProductProvider(CDITraceScope tracer, @Named("product") JedisPool pool) {
		this.tracer = tracer;
		this.pool = pool;
	}

	protected Jedis getJedis() {
		return pool.getResource();
	}

	/**
	 * 製品情報を登録・更新する
	 * @param product 更新する製品情報のオブジェクト
	 */
	@Traceable
	public void updateProduct(ProductEntity product) {
		Jedis client = getJedis();
		String productKey = PRODUCT_PREFIX + product.getId();

		Scope scope = tracer.begin("update_product");

		Map<String, String> itemMap = product.asMap();
		client.hmset(productKey, itemMap);
		client.sadd(PRICE_SORT_LIST, productKey);

		client.close();

		tracer.end(scope);
	}

	/**
	 * 商品情報を取得する
	 * @param productId 商品ID
	 * @return 商品情報のエンティティ
	 */
	@Traceable
	public ProductEntity getProduct(String productId) {
		Jedis client = getJedis();
		String productKey = PRODUCT_PREFIX + productId;

		Scope scope = tracer.begin("get_product");

		Map<String, String> itemMap = client.hgetAll(productKey);

		client.close();

		tracer.end(scope);

		return (itemMap != null ? ProductEntity.newInstance(itemMap) : null);
	}

	/**
	 * 商品の検索結果を取得する
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param descending 降順ソートの場合に true
	 * @return 検索結果のエンティティ
	 */
	@Traceable
	public SearchResultEntity searchProducts(int offset, int limit, boolean descending) {
		Jedis client = getJedis();
		SortingParams sp = new SortingParams().by("product:*->price");
		sp = (descending ? sp.desc() : sp.asc()).limit(offset, limit);

		Scope scope = tracer.begin("search_products");

		List<String> keyList = client.sort(PRICE_SORT_LIST, sp);
		Collection<ProductEntity> results = new ArrayList<ProductEntity>();
		for (String key : keyList) {
			results.add(ProductEntity.newInstance(client.hgetAll(key)));
		}

		SearchResultEntity result = SearchResultEntity.newInstance(results, offset);

		client.close();

		tracer.end(scope);

		return result;
	}
}
