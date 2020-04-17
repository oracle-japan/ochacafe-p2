package com.oracle.demo.mall.entity;

import java.util.Collection;

/**
 * 商品情報一覧のエンティティ
 */
public class SearchResultEntity {
	private Collection<ProductEntity> results;
	int offset = 0;
	int count = 0;

	@Deprecated
	protected SearchResultEntity() {

	}

	public Collection<ProductEntity> getResults() {
		return results;
	}
	public int getOffset() {
		return offset;
	}
	public int getCount() {
		return count;
	}
	public void setResults(Collection<ProductEntity> results) {
		this.results = results;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public void setCount(int count) {
		this.count = count;
	}

}
