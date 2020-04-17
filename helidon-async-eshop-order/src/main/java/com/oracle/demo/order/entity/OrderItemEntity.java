package com.oracle.demo.order.entity;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

/**
 * 注文アイテムを示すエンティティ
 */
@Access(value = AccessType.FIELD)
@PersistenceUnit(unitName = "OrderUnit")
@Entity(name = "OrderItemEntity")
@Table(name = "ORDER_ITEM_TABLE")
public class OrderItemEntity {
	@Id
	@Column(
		insertable = true,
		name = "ID",
		nullable = false,
		updatable = false
	)
	private String id;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "PRODUCT_ID",
			nullable = false,
			updatable = false
		)
	private String productId;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "PRODUCT_NAME",
			nullable = false,
			updatable = false
		)
	private String productName;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "PRICE",
			nullable = false,
			updatable = false
		)
	private long price;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "SUBTOTAL_PRICE",
			nullable = false,
			updatable = false
		)
	private long subtotalPrice;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "COUNT",
			nullable = false,
			updatable = true
		)
	private int count;

	@Basic(optional = false)
	@Column(
			insertable = false,
			name = "ORDER_ID",
			nullable = false,
			updatable = false
		)
	private String orderId;

	@ManyToOne
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
		this.orderId = order.getId();
		this.productId = productId;
		this.productName = productName;
		this.count = count;
		this.price = price;
		this.subtotalPrice = (price * count);
		this.id = this.orderId + "_" + this.productId;
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

	@JsonbTransient
	public String getOrderId() {
		return orderId;
	}

}
