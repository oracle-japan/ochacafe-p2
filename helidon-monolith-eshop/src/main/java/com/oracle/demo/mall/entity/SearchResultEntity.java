package com.oracle.demo.mall.entity;

import java.util.Collection;

/**
 * 商品検索結果を示すエンティティ
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
