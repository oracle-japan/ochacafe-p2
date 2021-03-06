package com.oracle.demo.cart.entity;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import com.oracle.demo.mall.entity.ProductEntity;

/**
 * ショッピングカート内のアイテムを示すエンティティ
 */
@Access(value = AccessType.FIELD)
@PersistenceUnit(unitName = "CartUnit")
@Entity(name = "CartItemEntity")
@Table(name = "CART_ITEM_TABLE")
public class CartItemEntity {

	/**
	 * カート内のアイテムに対する在庫情報をJoinするためのエンティティ
	 */
	@Access(value = AccessType.FIELD)
	@PersistenceUnit(unitName = "CartUnit")
	@Entity(name = "CartItemEntity$Inventory")
	@Table(name = "INVENTORY_TABLE")
	private static class Inventory {
		@Id
		@Column(
				insertable = true,
				name = "PRODUCT_ID",
				nullable = false,
				updatable = false
			)
		private String productId;

		@Basic(optional = false)
		@Column(
				insertable = false,
				name = "STOCK_COUNT",
				nullable = false,
				updatable = true
			)
		private int stockCount;

		@SuppressWarnings("unused")
		@Deprecated
		protected Inventory() {
		}

		public int getStockCount() {
			return stockCount;
		}

		public void setStockCount(int stockCount) {
			this.stockCount = stockCount;
		}
	}

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(
			name = "PRODUCT_ID",
			insertable=false,
			updatable=false)
	private Inventory inventory;


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
			name = "COUNT",
			nullable = false,
			updatable = true
		)
	private int count;

	@Basic(optional = false)
	@Column(
			insertable = false,
			name = "CART_ID",
			nullable = false,
			updatable = false
		)
	private String cartId;

	@ManyToOne
	private CartEntity cart;


	public static final CartItemEntity newInstance(CartEntity cart, ProductEntity product) {
		return newInstance(cart, product, 1);
	}
	public static final CartItemEntity newInstance(CartEntity cart, ProductEntity product, int count) {
		return new CartItemEntity(cart, product, count);
	}

	@Deprecated
	protected CartItemEntity() {
	}

	private CartItemEntity(CartEntity cart, ProductEntity product, int count) {
		this.cart = cart;
		this.cartId = cart.getId();
		this.productId = product.getId();
		this.productName = product.getName();
		this.price = product.getPrice();
		this.count = count;
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
		return this.inventory.getStockCount();
	}

	public void setStockCount(int stockCount) {
		this.inventory.setStockCount(stockCount);
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
