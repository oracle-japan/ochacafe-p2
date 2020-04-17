package com.oracle.demo.mall.entity;

import java.util.Collection;

/**
 * 商品検索結果のエンティティ
 */
public class SearchResultEntity {
	private Collection<ProductEntity> results;
	int offset;
	int count;

	@Deprecated
	protected SearchResultEntity() {
	}

	private SearchResultEntity(Collection<ProductEntity> results, int offset) {
		this.results = results;
		this.offset = offset;
		this.count = results.size();
	}

	public static final SearchResultEntity newInstance(Collection<ProductEntity> results, int offset) {
		return new SearchResultEntity(results, offset);
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
