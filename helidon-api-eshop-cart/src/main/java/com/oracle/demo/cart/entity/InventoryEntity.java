package com.oracle.demo.cart.entity;

import javax.json.bind.annotation.JsonbProperty;

/**
 * 在庫のみの情報を示すエンティティ
 */
public class InventoryEntity {
	private String productId;
	private int stockCount;

	@Deprecated
	protected InventoryEntity() {
	}

	@JsonbProperty("product_id")
	public String getProductId() {
		return productId;
	}

	@JsonbProperty("stock_count")
	public int getStockCount() {
		return stockCount;
	}

	@JsonbProperty("product_id")
	public void setProductId(String productId) {
		this.productId = productId;
	}

	@JsonbProperty("stock_count")
	public void setStockCount(int stockCount) {
		this.stockCount = stockCount;
	}

}
