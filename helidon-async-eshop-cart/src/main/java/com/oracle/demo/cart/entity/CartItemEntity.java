package com.oracle.demo.cart.entity;

import java.util.HashMap;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;

import com.oracle.demo.cart.entity.ProductEntity;

/**
 * カートアイテムのエンティティ
 */
public class CartItemEntity {

	// Redisに格納するHM作成
	public Map<String, String> asMap() {
		Map<String, String> result =  new HashMap<String, String>();
		result.put("id", this.id);
		result.put("product_id", this.productId);
		result.put("product_name", this.productName);
		result.put("price", Long.toString(this.price));
		result.put("count", Integer.toString(this.count));
		result.put("cart_id", this.cartId);

		return result;
	}

	private CartItemEntity(Map<String, String> map) {
		this.id = map.get("id");
		this.productId = map.get("product_id");
		this.productName = map.get("product_name");
		this.price = Long.parseLong(map.get("price"));
		this.count = Integer.parseInt(map.get("count"));
		this.cartId = map.get("cart_id");
	}


	private String id;
	private String productId;
	private String productName;
	private long price;
	private int count;
	private String cartId;
	private int stockCount;

	public static final CartItemEntity newInstance(CartEntity cart, ProductEntity product) {
		return newInstance(cart, product, 1);
	}
	public static final CartItemEntity newInstance(CartEntity cart, ProductEntity product, int count) {
		return new CartItemEntity(cart, product, count);
	}
	public static final CartItemEntity newInstance(Map<String, String> map) {
		return new CartItemEntity(map);
	}

	@Deprecated
	protected CartItemEntity() {
	}

	private CartItemEntity(CartEntity cart, ProductEntity product, int count) {
		this.cartId = cart.getId();
		this.count = count;
		this.productId = product.getId();
		this.productName = product.getName();
		this.price = product.getPrice();
		this.stockCount = product.getStockCount();

		this.id = this.cartId + "_" + this.productId;
}

	@JsonbTransient
	public String getId() {
		return id;
	}

	@JsonbProperty("product_id")
	public String getProductId() {
		return productId;
	}

	@JsonbProperty("product_name")
	public String getProductName() {
		return productName;
	}


	public long getPrice() {
		return price;
	}


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@JsonbProperty("cart_id")
	public String getCartId() {
		return cartId;
	}

	@JsonbProperty("stock_count")
	public int getStockCount() {
		return this.stockCount;
	}

	public void updateStockCount(int stockCount) {
		this.stockCount = stockCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null) {
			return (obj instanceof CartItemEntity) ?
					((CartItemEntity) obj).productId.equals(this.productId) :
					false;
		} else {
			return false;
		}
	}


}
