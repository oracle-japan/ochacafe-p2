package com.oracle.demo.order.entity;

import java.util.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

/**
 * 注文情報のエンティティ
 */
@Access(value = AccessType.FIELD)
@PersistenceUnit(unitName = "OrderUnit")
@Entity(name = "OrderEntity")
@Table(name = "ORDER_TABLE")
@NamedQueries({
	@NamedQuery(name = "OrderEntity.All.Ascending", query = "select o from OrderEntity o order by o.orderDate asc"),
	@NamedQuery(name = "OrderEntity.All.Descending", query = "select o from OrderEntity o order by o.orderDate desc")
})
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
			name = "USER_ID",
			nullable = false,
			updatable = false
		)
	private String userId;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "ORDER_DATE",
			nullable = false,
			updatable = false
		)
	private Date orderDate;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "TOTAL_PRICE",
			nullable = false,
			updatable = true
		)
	private long totalPrice;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "STATUS",
			nullable = false,
			updatable = true
		)
	@Enumerated(EnumType.STRING)
	private Status status;

	@OneToMany(
			mappedBy = "order",
			cascade = {CascadeType.ALL},
			orphanRemoval = true
		)
	private Collection<OrderItemEntity> items;

	@Deprecated
	protected OrderEntity() {
	}

	private OrderEntity(String id, String userId, Date orderDate) {
		this.id = id;
		this.userId = userId;
		this.orderDate = orderDate;
		this.totalPrice = 0;
		this.status = Status.Ordered;
	}

	public static final OrderEntity newInstance(String userId) {
		String id = UUID.randomUUID().toString();
		Date orderDate = new Date(System.currentTimeMillis());
		return new OrderEntity(id, userId, orderDate);
	}

	public void addItem(String productId,
			String productName,
			int count,
			long price
			) {
		OrderItemEntity item = OrderItemEntity.newInstance(this, productId, productName, count, price);
		this.items.add(item);
		this.totalPrice += item.getSubtotalPrice();
	}

	public String getId() {
		return id;
	}

	@JsonbProperty("user_id")
	public String getUserId() {
		return userId;
	}

	/**
	 * JSONフォーマットでシステムのタイムゾーン指定で返却するためのgetter
	 * @return システム・タイムゾーンでの注文日付
	 */
	@JsonbProperty("order_date")
	public ZonedDateTime getZonedDataTime() {
		return this.orderDate.toInstant().atZone(ZoneId.systemDefault());
	}

	@JsonbTransient
	public Date getOrderDate() {
		return orderDate;
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


}
