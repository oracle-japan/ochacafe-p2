package com.oracle.demo.cart;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.oracle.demo.cart.entity.CartEntity;
import com.oracle.demo.order.entity.OrderEntity;

/**
 * カート(Cart)のREST APIを提供するRESTアプリケーションリソース
 */
@Path("/cart")
@RequestScoped
@Timed(name = "timer", unit = MetricUnits.MICROSECONDS)
@Counted(name = "counter")
@Metered(name = "meter")
public class CartResource {
	private CartProvider provider;

	@Inject
	protected CartResource(CartProvider provider) {
		this.provider = provider;
	}

	/**
	 * カートを作成する
	 * @param userId カートを作成するユーザID。
	 * @return 作成後のカートの内容のJsonObject
	 */
	@POST
	@Path("/{user-id}")
    @Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity create(@PathParam("user-id") String userId) {
		try {
			CartEntity cart = this.provider.createCart(userId);

			if(cart == null) {
				String errorMessage = String.format("Resource for user-id \"%s\" not found", userId);
				throw new NotFoundException(errorMessage);
			}

			return cart;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容を取得する。
	 * @param userId カートのID。
	 * @return カートの内容のJsonObject
	 */
	@GET
	@Path("/{cart-id}")
    @Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity get(@PathParam("cart-id") String cartId) {
		try {
			CartEntity cart = this.provider.getCart(cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				throw new NotFoundException(errorMessage);
			}

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
	 * @return 追加後のカートの内容のJsonObject
	 */
	@POST
	@Path("/{cart-id}/{product-id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity insertItem(@PathParam("cart-id") String cartId, @PathParam("product-id") String productId) {
		try {
			CartEntity cart = this.provider.insertItem(cartId, productId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				throw new NotFoundException(errorMessage);
			}

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
	@DELETE
	@Path("/{cart-id}/{product-id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity deleteItem(@PathParam("cart-id") String cartId, @PathParam("product-id") String productId, @DefaultValue("0") @QueryParam("count") int count) {
		try {
			CartEntity cart = this.provider.deleteItem(cartId, productId, count);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				throw new NotFoundException(errorMessage);
			}

			return cart;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容を全て削除する
	 * @param cartId カートID
	 * @return 削除後のカートの内容のJsonObject
	 */
	@DELETE
	@Path("/{cart-id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public CartEntity clearCart(@PathParam("cart-id") String cartId) {
		try {
			CartEntity cart = this.provider.clearCart(cartId);

			if(cart == null) {
				String errorMessage = String.format("Resource for cart-id \"%s\" not found", cartId);
				throw new NotFoundException(errorMessage);
			}

			return cart;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * カートの内容をチェックアウト(注文)する。
	 * @param userId
	 * @return 注文情報
	 */
	@POST
	@Path("/checkout/{user-id}")
    @Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public OrderEntity checkout(@PathParam("user-id") String userId) {
		try {
			OrderEntity order = this.provider.checkout(userId);

			if(order == null) {
				String errorMessage = String.format("Resource for user-id \"%s\" not found", userId);
				throw new NotFoundException(errorMessage);
			}

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}
