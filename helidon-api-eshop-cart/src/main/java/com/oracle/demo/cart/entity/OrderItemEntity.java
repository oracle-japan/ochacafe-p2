package com.oracle.demo.cart.entity;

import javax.json.bind.annotation.JsonbProperty;

/**
 * 注文アイテムのエンティティ
 */
public class OrderItemEntity {
	private String id;
	private String productId;
	private String productName;
	private long price;
	private long subtotalPrice;
	private int count;

	@Deprecated
	protected OrderItemEntity() {
	}

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

	@JsonbProperty("subtotal_price")
	public long getSubtotalPrice() {
		return subtotalPrice;
	}

	public int getCount() {
		return count;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonbProperty("product_id")
	public void setProductId(String productId) {
		this.productId = productId;
	}

	@JsonbProperty("product_name")
	public void setProductName(String productName) {
		this.productName = productName;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	@JsonbProperty("subtotal_price")
	public void setSubtotalPrice(long subtotalPrice) {
		this.subtotalPrice = subtotalPrice;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
