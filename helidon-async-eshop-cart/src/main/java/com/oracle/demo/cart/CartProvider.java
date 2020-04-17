package com.oracle.demo.cart;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.oracle.demo.cart.entity.CartEntity;
import com.oracle.demo.cart.entity.CartItemEntity;
import com.oracle.demo.cart.entity.OrderEntity;
import com.oracle.demo.cart.entity.ProductEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * カートの操作を行うビジネスロジックを集約するプロバイダ・クラス
 */
@ApplicationScoped
public class CartProvider {
	private static final String CART_PREFIX= "cart:";
	private static final String CART_ITEMLIST_PREFIX= "cart_list:";
	private static final String CART_ITEM_PREFIX= "cart_item:";

	private JedisPool pool;

	private CDITraceScope tracer;
	private ProductProvider productProvider;
	private OrderProvider orderProvider;

	@Inject
	protected CartProvider(CDITraceScope tracer, @Named("cart") JedisPool pool,
			ProductProvider productProvider, OrderProvider orderProvider) {
		this.tracer = tracer;
		this.pool = pool;
		this.productProvider = productProvider;
		this.orderProvider = orderProvider;
	}

	private Jedis getJedis() {
		return pool.getResource();
	}

	/**
	 * カートを作成する
	 * @param userId カートを作成するユーザID。
	 * @return 作成後のカートの内容のJsonObject
	 */
	@Traceable
	public CartEntity createCart(String cartId) {
		Jedis client = getJedis();

		Scope scope = tracer.begin("create_cart");

		CartEntity cart = CartEntity.newInstance(cartId);
		client.set(CART_PREFIX + cartId, cartId);

		client.close();

		tracer.end(scope);

		return cart;
	}

	/**
	 * カートの内容を取得する。
	 * @param urId カートのID。
	 * @return カートの内容のJsonObject
	 */
	@Traceable
	public CartEntity getCart(String cartId) {
		CartEntity cart = null;
		Jedis client = getJedis();
		String cartKey = CART_PREFIX + cartId;
		String cartListKey = CART_ITEMLIST_PREFIX + cartId;

		Scope scope = tracer.begin("get_cart");

		if (client.get(cartKey) != null) { // カートが存在する場合
			cart = CartEntity.newInstance(cartId);
			Set<String> list = client.smembers(cartListKey);

			for (String itemId : list) {
				Map<String, String> itemMap = client.hgetAll(itemId);
				CartItemEntity item = CartItemEntity.newInstance(itemMap);

				// 商品情報から在庫数を更新
				ProductEntity product = productProvider.getProduct(item.getProductId());
				if (product != null) { // 商品情報が欠落している場合はとりあえず在庫を更新しない
					item.updateStockCount(product.getStockCount());
				}
				cart.addItem(item);
			}
		}

		client.close();

		tracer.end(scope);

		return cart;
	}

	/**
	 * カートの内容を削除する
	 * @param cartId カートID
	 * @param 削除する商品のID
	 * @return 削除後のカートの内容のJsonObject
	 */
	@Traceable
	public CartEntity insertItem(String cartId, String productId) {
		try {
			Scope scope = tracer.begin("add_cart");

			CartEntity cart = getCart(cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				tracer.end(scope, errorMessage);
				return null;
			}

			// order側にRESTで取得
			ProductEntity product = productProvider.getProduct(productId);

			if(product == null) {
				String errorMessage = String.format("Resource for product-id \"%s\" not found", productId);
				tracer.end(scope, errorMessage);
				return null;
			}

			cart.addItem(CartItemEntity.newInstance(cart, product));
			updateCart(cart);

			tracer.end(scope);

			return cart;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容を削除する
	 * @param cartId カートID
	 * @param 削除する商品のID
	 * @return 削除後のカートの内容のJsonObject
	 */
	@Traceable
	public CartEntity deleteItem(String cartId, String productId, int count) {
		try {
			Scope scope = tracer.begin("remove_cart");

			CartEntity cart = getCart(cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				tracer.end(scope, errorMessage);
				return null;
			}

			cart.removeItem(productId, count);
			updateCart(cart);

			tracer.end(scope);

			return cart;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容を全て削除する
	 * @param cartId カートID
	 * @return 削除後のカートの内容
	 */
	@Traceable
	public CartEntity clearCart(String cartId) {
		Jedis client = getJedis();
		String cartKey = CART_PREFIX + cartId;
		String cartListKey = CART_ITEMLIST_PREFIX + cartId;

		Scope scope = tracer.begin("clear_cart");

		if (client.get(cartKey) != null) { // カートが存在する場合のみ

			Set<String> list = client.smembers(cartListKey);
			if (list != null) { //　カートが空の場合は何もしない
				client.del(list.toArray(new String[0]));
				client.del(cartListKey);
			}
		}

		client.close();

		tracer.end(scope);

		return getCart(cartId);
	}

	/**
	 * カートの内容をチェックアウト(注文)する。
	 * @param userId
	 * @return 注文情報
	 */
	@Traceable
	public OrderEntity checkout(String userId) {
		try {
			Scope scope = tracer.begin("checkout_order");

			CartEntity cart = getCart(userId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", userId);
				tracer.end(scope, errorMessage);
				return null;
			}

			if(cart.getCount() == 0) {
				String errorMessage = String.format("Cart cart-id \"%s\" is empty", userId);
				tracer.end(scope, errorMessage);
				return null;
			}

			OrderEntity order = orderProvider.checkout(cart);

			if(order != null) {
				clearCart(userId);
			} else {
				String errorMessage = String.format("Checkout is failed for cart-id \"%s\"", userId);
				tracer.end(scope, errorMessage);
				return null;
			}

			tracer.end(scope);

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの情報を更新する。
	 * @param cart 更新するカートのエンティティ
	 * @return 更新後のカートのエンティティ
	 */
	private CartEntity updateCart(CartEntity cart) {
		Jedis client = getJedis();
		String cartKey = CART_PREFIX + cart.getId();
		String cartListKey = CART_ITEMLIST_PREFIX + cart.getId();

		Scope scope = tracer.begin("update_cart");

		if (client.get(cartKey) != null) { // カートが存在する場合のみ

			Set<String> list = client.smembers(cartListKey);
			if (list == null) {
				list = new HashSet<String>();
			}

			Set<String> updateList = new HashSet<String>(); // 上書き対象になるリスト
			for (CartItemEntity item : cart.getItems()) {
				updateList.add(CART_ITEM_PREFIX + item.getId());
				Map<String, String> itemMap = item.asMap();
				client.hmset(CART_ITEM_PREFIX + item.getId(), itemMap);
			}

			list.removeAll(updateList); // 削除対象のリスト
			if (list.size() > 0) {
				for (String removeItem : list) {
					client.del(removeItem);
				}
				client.srem(cartListKey, list.toArray(new String[0]));
			}

			client.sadd(cartListKey, updateList.toArray(new String[0]));
		}

		client.close();

		tracer.end(scope);

		return cart;
	}

}
