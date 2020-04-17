package com.oracle.demo.cart.entity;

import javax.json.bind.annotation.JsonbProperty;

import com.oracle.demo.cart.entity.OrderEntity;
import com.oracle.demo.cart.entity.OrderItemEntity;

/**
 * 注文アイテムのエンティティ
 */
public class OrderItemEntity {
	private String productId;
	private String productName;
	private long price;
	private int count;
	private OrderEntity order;

	@Deprecated
	protected OrderItemEntity() {
	}

	private OrderItemEntity(OrderEntity order,
			String productId,
			String productName,
			int count,
			long price
			) {
		this.order = order;
		this.productId = productId;
		this.productName = productName;
		this.count = count;
		this.price = price;
	}

	public static final OrderItemEntity newInstance(
			OrderEntity order,
			String productId,
			String productName,
			int count,
			long price
			) {
		return new OrderItemEntity(order, productId, productName, count, price);
	}

	public String getId() {
		return order.getId() + "_" + this.productId;
	}

	public String getOrderId() {
		return order.getId();
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
		return (this.price * this.count);
	}

	public int getCount() {
		return count;
	}
}
