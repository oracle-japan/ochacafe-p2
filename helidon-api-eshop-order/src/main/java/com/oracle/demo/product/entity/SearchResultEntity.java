package com.oracle.demo.product.entity;

import java.util.Collection;

/**
 * 商品一覧を示すエンティティ
 */
public class SearchResultEntity {
	private Collection<ProductEntity> results;
	int offset = 0;

	public SearchResultEntity(Collection<ProductEntity> results, int offset) {
		this.results = results;
		this.offset = offset;
	}

	public Collection<ProductEntity> getResults() {
		return this.results;
	}

	public int getCount() {
		return this.results.size();
	}

	public int getOffset() {
		return this.offset;
	}
}
