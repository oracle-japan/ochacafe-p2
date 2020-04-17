package com.oracle.demo.order.entity;

import java.util.Collection;

/**
 * 注文情報の検索結果を示すエンティティ
 */
public class SearchResultEntity {
	private Collection<OrderEntity> results;
	int offset = 0;

	public SearchResultEntity(Collection<OrderEntity> results, int offset) {
		this.results = results;
		this.offset = offset;
	}

	public Collection<OrderEntity> getResults() {
		return this.results;
	}

	public int getCount() {
		return this.results.size();
	}

	public int getOffset() {
		return this.offset;
	}
}
