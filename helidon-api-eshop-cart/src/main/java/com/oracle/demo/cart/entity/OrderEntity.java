package com.oracle.demo.cart.entity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;

import javax.json.bind.annotation.JsonbProperty;

/**
 * 注文情報のエンティティ
 */
public class OrderEntity {
	/**
	 * 注文ステータスを示すEnum
	 */
	public enum Status {
		Ordered("ordered"),
		Cancelled("cancelled"),
		Shipped("shipped"),
		Delivered("delivered"),
		Suspended("suspended"),
		Unknown("unknown");

		private String status;
		private Status(String status) {
			this.status = status;
		}

		public static Status toStatus(String value) {
			for (Status result : values()) {
				if (result.status.equals(value.toLowerCase())) {
					return result;
				}
			}
			return Unknown;
		}

	};

	private String id;
	private String userId;
	private ZonedDateTime orderDate;
	private long totalPrice;
	private Status status;
	private Collection<OrderItemEntity> items;

	@Deprecated
	protected OrderEntity() {
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

	@JsonbProperty("status")
	public Status getStatus() {
		return status;
	}

	@JsonbProperty("status")
	public void setStatus(Status status) {
		this.status = status;
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

	@JsonbProperty("order_date")
	public void setOrderDate(ZonedDateTime orderDate) {
		this.orderDate = orderDate;
	}

	@JsonbProperty("total_price")
	public void setTotalPrice(long totalPrice) {
		this.totalPrice = totalPrice;
	}

	public void setItems(Collection<OrderItemEntity> items) {
		this.items = items;
	}


}
