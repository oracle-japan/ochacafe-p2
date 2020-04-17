package com.oracle.demo.product.entity;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

/**
 * 商品情報を示すのエンティティ
 */
@Access(value = AccessType.FIELD)
@PersistenceUnit(unitName = "ProductUnit")
@Entity(name = "ProductEntity")
@Table(name = "PRODUCT_TABLE")
@NamedQueries({
	@NamedQuery(name = "ProductEntity.All.Ascending", query = "select p from ProductEntity p order by p.price asc"),
	@NamedQuery(name = "ProductEntity.All.Descending", query = "select p from ProductEntity p order by p.price desc")
})
public class ProductEntity {

	/**
	 * 在庫情報をJoinするためのエンティティ
	 */
	@Access(value = AccessType.FIELD)
	@PersistenceUnit(unitName = "ProductUnit")
	@Entity(name = "ProductEntity$Inventory")
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

		@Deprecated
		@SuppressWarnings("unused")
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
			name = "ID",
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
			name = "NAME",
			nullable = false,
			updatable = false
		)
	private String name;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "VARIATION",
			nullable = false,
			updatable = false
		)
	private String variation;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "CATEGORY_ID",
			nullable = false,
			updatable = false
		)
	private String categoryId;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "CATEGORY_NAME",
			nullable = false,
			updatable = false
		)
	private String categoryName;

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
			name = "SUPPLIER_ID",
			nullable = false,
			updatable = false
		)
	private String supplierId;

	@Basic(optional = false)
	@Column(
			insertable = true,
			name = "SUPPLIER_NAME",
			nullable = false,
			updatable = false
		)
	private String supplierName;

	@Deprecated
	protected ProductEntity() {
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getVariation() {
		return variation;
	}

	@JsonbProperty("category_id")
	public String getCategoryId() {
		return categoryId;
	}

	@JsonbProperty("category_name")
	public String getCategoryName() {
		return categoryName;
	}

	public long getPrice() {
		return price;
	}

	@JsonbProperty("supplier_id")
	public String getSupplierId() {
		return supplierId;
	}

	@JsonbProperty("supplier_name")
	public String getSupplierName() {
		return supplierName;
	}

	@JsonbProperty("stock_count")
	public int getStockCount() {
		return this.inventory.getStockCount();
	}

	@JsonbTransient
	public void setStockCount(int stockCount) {
		this.inventory.setStockCount(stockCount);
	}

}
