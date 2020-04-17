package com.oracle.demo.cart;

import java.util.HashMap;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.oracle.demo.cart.entity.CartEntity;
import com.oracle.demo.cart.entity.CartItemEntity;
import com.oracle.demo.cart.entity.OrderEntity;
import com.oracle.demo.cart.entity.OrderItemEntity;
import com.oracle.demo.cart.entity.ProductEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;

/**
 * カート操作のビジネスロジックを集約するプロバイダ・クラス
 */
@Dependent
public class CartProvider {
	@PersistenceContext(unitName = "CartUnit")
	private EntityManager em;

	private CDITraceScope tracer;
	private ProductProvider productProvider;
	private OrderProvider orderProvider;

	@Inject
	protected CartProvider(CDITraceScope tracer,
			ProductProvider productProvider,
			OrderProvider orderProvider) {
		this.tracer = tracer;
		this.productProvider = productProvider;
		this.orderProvider = orderProvider;
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
	 * @return カートのエンティティ
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

			productProvider.updateStockCount(cart);	// 在庫情報をOrder側の情報から更新

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

			// order側にRESTで取得
			ProductEntity product = this.productProvider.getProduct(productId);

			if(product == null) {
				String errorMessage = String.format("Resource for product-id \"%s\" not found", productId);
				tracer.end(scope, errorMessage);
				return null;
			}

			cart.addItem(CartItemEntity.newInstance(cart, product));
			em.merge(cart);
			em.flush();

			productProvider.updateStockCount(cart);	// 他のアイテムの在庫情報をOrder側の情報から更新

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
	 * @return 削除後のカートのエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity deleteItem(String cartId,  String productId, int count) {
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

			this.productProvider.updateStockCount(cart);	// 在庫情報をOrder側の情報から更新

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

			HashMap<String, Integer> orderList = new HashMap<String, Integer>();
			for (CartItemEntity cartItem : cart.getItems()) {
				orderList.put(cartItem.getProductId(), cartItem.getCount());
			}
			OrderEntity order = this.orderProvider.checkout(userId, orderList);

			for (OrderItemEntity orderItem : order.getItems()) {
				cart.removeItem(orderItem.getProductId(), 0);	// カートから注文済みのアイテムを削除
			}

			em.persist(cart);
			em.flush();
			em.refresh(cart);

			tracer.end(scope);

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


}
