package com.oracle.demo.mall.entity;

import java.util.HashMap;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;

import com.oracle.demo.mall.entity.ProductEntity;

/**
 * 商品情報のエンティティ
 */
public class ProductEntity {

	// Redisに格納するHM作成
	public Map<String, String> asMap() {
		Map<String, String> result =  new HashMap<String, String>();
		result.put("id", this.id);
		result.put("name", this.name);
		result.put("price", Long.toString(this.price));
		result.put("stock_count", Integer.toString(this.stockCount));
		result.put("variation", this.variation);
		result.put("supplier_id", this.supplierId);
		result.put("supplier_name", this.supplierName);
		result.put("category_id", this.categoryId);
		result.put("category_name", this.categoryName);

		return result;
	}

	private ProductEntity(Map<String, String> map) {
		this.id = map.get("id");
		this.name = map.get("name");
		this.price = Long.parseLong(map.get("price"));
		this.stockCount = Integer.parseInt(map.get("stock_count"));
		this.variation = map.get("variation");
		this.supplierId = map.get("supplier_id");
		this.supplierName = map.get("supplier_name");
		this.categoryId = map.get("category_id");
		this.categoryName = map.get("category_name");
	}

	private String id;
	private String name;
	private String variation;
	private String categoryId;
	private String categoryName;
	private long price;
	private String supplierId;
	private String supplierName;
	private int stockCount;

	@Deprecated
	protected ProductEntity() {
	}

	public static final ProductEntity newInstance(Map<String, String> map) {
		return new ProductEntity(map);
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
