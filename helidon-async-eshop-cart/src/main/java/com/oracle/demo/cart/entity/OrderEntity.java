package com.oracle.demo.cart.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import javax.json.bind.annotation.JsonbProperty;

/**
 * 注文情報のエンティティ
 */
public class OrderEntity {
	private String id;
	private String userId;
	private ZonedDateTime orderDate;
	private long totalPrice;
	private Collection<OrderItemEntity> items;

	@Deprecated
	protected OrderEntity() {
	}

	private OrderEntity(String userId) {
		this.id = UUID.randomUUID().toString();
		this.userId = userId;
		this.orderDate = new Date().toInstant().atZone(ZoneId.systemDefault());
		this.totalPrice = 0;
		this.items = new HashSet<OrderItemEntity>();
	}

	public static final OrderEntity newInstance(String userId) {
		return new OrderEntity(userId);
	}

	public String getId() {
		return id;
	}

	@JsonbProperty("user_id")
	public String getUserId() {
		return userId;
	}

	@JsonbProperty("order_date")
	public ZonedDateTime getOrderDate() {
		return this.orderDate.toInstant().atZone(ZoneId.systemDefault());
	}

	@JsonbProperty("total_price")
	public long getTotalPrice() {
		return totalPrice;
	}

	public Collection<OrderItemEntity> getItems() {
		return items;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonbProperty("user_id")
	public void setUserId(String userId) {
		this.userId = userId;
	}

	@JsonbProperty("total_price")
	public void setTotalPrice(long totalPrice) {
		this.totalPrice = totalPrice;
	}

	public void setItems(Collection<OrderItemEntity> items) {
		this.items = items;
	}


}
