package com.oracle.demo.product.entity;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

/**
 * 在庫情報を示すエンティティ
 */
@Access(value = AccessType.FIELD)
@PersistenceUnit(unitName = "ProductUnit")
@Entity(name = "InventoryEntity")
@Table(name = "INVENTORY_TABLE")
public class InventoryEntity {
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
	protected InventoryEntity() {
	}

	private InventoryEntity(String productId) {
		this.productId = productId;
		this.stockCount = 0;
	}

	public static final InventoryEntity emptyInventory(String productId) {
		return new InventoryEntity(productId);
	}

	@JsonbProperty("product_id")
	public String getProductId() {
		return productId;
	}

	@JsonbProperty("stock_count")
	public int getStockCount() {
		return stockCount;
	}

	@JsonbTransient
	public void setProductId(String productId) {
		this.productId = productId;
	}

	@JsonbTransient
	public void setStockCount(int stockCount) {
		this.stockCount = stockCount;
	}
}
