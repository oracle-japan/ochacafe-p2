package com.oracle.demo.order.entity;

import java.time.ZonedDateTime;
import java.util.Collection;

import javax.json.bind.annotation.JsonbProperty;

/**
 * 注文エントリを示すエンティティ
 */
public class OrderEntryEntity {

	public static class Item {
		private String productId;
		private int count;

		@Deprecated
		protected Item() {
		}

		@JsonbProperty("product_id")
		public String getProductId() {
			return productId;
		}

		public int getCount() {
			return count;
		}

		@JsonbProperty("product_id")
		public void setProductId(String productId) {
			this.productId = productId;
		}

		public void setCount(int count) {
			this.count = count;
		}
	}

	private String id;
	private String userId;
	private ZonedDateTime orderDate;
	private Collection<Item> items;

	@Deprecated
	protected OrderEntryEntity() {
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
		return orderDate;
	}

	public Collection<Item> getItems() {
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

	public void setItems(Collection<Item> items) {
		this.items = items;
	}
}
