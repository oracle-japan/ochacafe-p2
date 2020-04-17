package com.oracle.demo.cart.entity;

import java.util.HashMap;
import java.util.Map;

import javax.json.bind.annotation.JsonbProperty;

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

		return result;
	}

	private ProductEntity(Map<String, String> map) {
		this.id = map.get("id");
		this.name = map.get("name");
		this.price = Long.parseLong(map.get("price"));
		this.stockCount = Integer.parseInt(map.get("stock_count"));
	}

	private String id;
	private String name;
	private long price;
	private int stockCount;

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

	public long getPrice() {
		return price;
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

	public void setPrice(long price) {
		this.price = price;
	}

	@JsonbProperty("stock_count")
	public void setStockCount(int stockCount) {
		this.stockCount = stockCount;
	}

}
