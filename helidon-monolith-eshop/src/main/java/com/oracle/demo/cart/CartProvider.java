package com.oracle.demo.cart;

import java.util.ArrayList;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.oracle.demo.cart.entity.CartEntity;
import com.oracle.demo.cart.entity.CartItemEntity;
import com.oracle.demo.mall.entity.ProductEntity;
import com.oracle.demo.order.entity.OrderEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;

/**
 * カート情報の取得・操作のビジネスロジックを集約したプロバイダ・クラス
 */
@Dependent
public class CartProvider {
	@PersistenceContext(unitName = "CartUnit")
	private EntityManager em;

	@PersistenceContext(unitName = "ProductUnit")
	private EntityManager productEm;

	@PersistenceContext(unitName = "OrderUnit")
	private EntityManager orderEm;

	private CDITraceScope tracer;

	@Inject
	protected CartProvider(CDITraceScope tracer) {
		this.tracer = tracer;
	}

	/**
	 * カートを作成する
	 * @param userId カートを作成するユーザID。
	 * @return 作成後のカートのエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity createCart(String userId) {
		try {
			CartEntity cart = CartEntity.newInstance(userId);

			Scope scope = tracer.begin("create_cart");

			em.persist(cart);
			em.flush();

			tracer.end(scope);

			return cart;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容を取得する。
	 * @param userId カートのID。
	 * @return 取得したカートのエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity getCart(String cartId) {
		try {
			Scope scope = tracer.begin("get_cart");

			CartEntity cart = em.find(CartEntity.class, cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				tracer.end(scope, errorMessage);
				return null;
			}

			tracer.end(scope);

			return cart;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートに商品を追加する
	 * @param cartId カートID
	 * @param productId 商品ID
	 * @return 追加後のカートのエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity insertItem(String cartId, String productId) {
		try {
			Scope scope = tracer.begin("add_cart");

			CartEntity cart = em.find(CartEntity.class, cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				tracer.end(scope, errorMessage);
				return null;
			}

			ProductEntity product = productEm.find(ProductEntity.class, productId);

			if(product == null) {
				String errorMessage = String.format("Resource for product-id \"%s\" not found", productId);
				tracer.end(scope, errorMessage);
				return null;
			}

			cart.addItem(CartItemEntity.newInstance(cart, product));
			em.merge(cart);
			em.flush();
			em.refresh(cart);

			cart = em.find(CartEntity.class, cartId);

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
	 * @param productId 削除する商品のID
	 * @param count 削除する商品の個数。0 の場合は全て削除
	 * @return 削除後のカートのエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity deleteItem(String cartId, String productId, int count) {
		try {
			Scope scope = tracer.begin("remove_cart");

			CartEntity cart = em.find(CartEntity.class, cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				tracer.end(scope, errorMessage);
				return null;
			}

			cart.removeItem(productId, count);
			em.merge(cart);
			em.flush();

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
	 * @return 削除後のカートのエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity clearCart(String cartId) {
		try {
			Scope scope = tracer.begin("clear_cart");

			CartEntity cart = em.find(CartEntity.class, cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				tracer.end(scope, errorMessage);
				return null;
			}

			cart.removeAll();
			em.merge(cart);
			em.flush();

			tracer.end(scope);

			return cart;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容をチェックアウト(注文)する。
	 * @param userId
	 * @return 注文情報のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public OrderEntity checkout(String userId) {
		try {
			ArrayList<CartItemEntity> orderedItems = new ArrayList<CartItemEntity>();

			Scope scope = tracer.begin("checkout_order");

			CartEntity cart = em.find(CartEntity.class, userId); // cart_id = user_id

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", userId);
				tracer.end(scope, errorMessage);
				return null;
			}

			if (cart.getCount() == 0) {
				String errorMessage = String.format("Cart cart-id \"%s\" is empty", userId);
				tracer.end(scope, errorMessage);
				return null;
			}

			OrderEntity order = OrderEntity.newInstance(userId);
			orderEm.persist(order);

			for (CartItemEntity item : cart.getItems()) {
				int stockCount = item.getStockCount() - item.getCount();
				if (stockCount >= 0) { // 在庫の方が多い場合のみ注文リストに入れる
					order.addItem(item.getProductId(), item.getProductName(), item.getCount(), item.getPrice());
					item.setStockCount(stockCount);
					orderedItems.add(item);
				}
			}

			cart.removeItems(orderedItems);

			em.persist(cart);
			em.flush();
			em.refresh(cart);

			orderEm.persist(order);
			orderEm.flush();
			orderEm.refresh(order);

			tracer.end(scope);

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


}
