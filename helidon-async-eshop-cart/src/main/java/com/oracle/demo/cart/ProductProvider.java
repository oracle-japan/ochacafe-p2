package com.oracle.demo.cart;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import com.oracle.demo.cart.entity.ProductEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 商品情報の操作のビジネスロジックを集約したプロバイダ・クラス
 */
@Dependent
public class ProductProvider {
	private static final String PRODUCT_PREFIX = "product:";
	private CDITraceScope tracer;
	private JedisPool pool;

	@Inject
	protected ProductProvider(CDITraceScope tracer,	@Named("product") JedisPool pool) {
		this.tracer = tracer;
		this.pool = pool;
	}

	protected Jedis getJedis() {
		return pool.getResource();
	}

	/**
	 * 商品情報を更新する。
	 * @param product 更新する商品情報のエンティティ
	 */
	@Traceable
	public void updateProduct(ProductEntity product) {
		Jedis client = getJedis();
		String productKey = PRODUCT_PREFIX + product.getId();

		Scope scope = tracer.begin("create_cart");

		Map<String, String> itemMap = product.asMap();
		client.hmset(productKey, itemMap);

		client.close();

		tracer.end(scope);
	}

	/**
	 * 商品情報を取得する
	 * @param productId　商品ID
	 * @return 商品情報のエンティティ
	 */
	@Traceable
	public ProductEntity getProduct(String productId) {
		Jedis client = getJedis();
		String productKey = PRODUCT_PREFIX + productId;

		Scope scope = tracer.begin("create_cart");

		Map<String, String> itemMap = client.hgetAll(productKey);

		client.close();

		tracer.end(scope);

		return (itemMap != null ? ProductEntity.newInstance(itemMap) : null);
	}
}
