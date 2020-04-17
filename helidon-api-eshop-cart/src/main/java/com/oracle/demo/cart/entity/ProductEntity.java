package com.oracle.demo.cart.entity;

import javax.json.bind.annotation.JsonbProperty;

/**
 * 商品情報のエンティティ
 */
public class ProductEntity {
	private String id;
	private String name;
	private String variation;
	private String categoryId;
	private String categoryName;
	private long price;
	private String supplierId;
	private String supplierName;
	private int stockCount;


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
		return stockCount;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVariation(String variation) {
		this.variation = variation;
	}

	@JsonbProperty("category_id")
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	@JsonbProperty("category_name")
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	@JsonbProperty("supplier_id")
	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	@JsonbProperty("supplier_name")
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	@JsonbProperty("stock_count")
	public void setStockCount(int stockCount) {
		this.stockCount = stockCount;
	}

}
