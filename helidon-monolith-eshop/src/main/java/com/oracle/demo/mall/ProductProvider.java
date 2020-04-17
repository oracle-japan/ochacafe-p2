package com.oracle.demo.mall;

import java.util.Collection;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.oracle.demo.mall.entity.ProductEntity;
import com.oracle.demo.mall.entity.SearchResultEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;


/**
 * 商品検索・商品情報の取得のビジネスロジックを集約したプロバイダ・クラス
 */
@Dependent
public class ProductProvider {
	@PersistenceContext(unitName = "ProductUnit")
	private EntityManager em;

	private CDITraceScope tracer;

	@Inject
	protected ProductProvider(CDITraceScope tracer) {
		this.tracer = tracer;
	}

	/**
	 * 商品検索の一覧を取得する。
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param desceding 降順で取得する場合にtrue。
	 * @return
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public SearchResultEntity search(int offset, int limit, boolean desceding) {

		Collection<ProductEntity> results = null;

		try {
			String namedQuery = (desceding ? "ProductEntity.All.Descending" : "ProductEntity.All.Ascending");

			Scope scope = tracer.begin("search_products");

			TypedQuery<ProductEntity> query = em.createNamedQuery(namedQuery, ProductEntity.class)
					.setFirstResult(offset);
			results = (limit > 0 ? query.setMaxResults(limit) : query).getResultList();

			tracer.end(scope);

			return new SearchResultEntity(results, offset);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 商品情報を取得する。
	 * @param productId 取得する商品の商品ID
	 * @return 商品情報のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public ProductEntity getProduct(String productId) {
		try {
			Scope scope = tracer.begin("get_product");

			ProductEntity product = em.find(ProductEntity.class,  productId);

			if(product == null) {
				String errorMessage =String.format("Resource for product-id \"%s\" not found", productId);
				tracer.end(scope, errorMessage);
				return null;
			}

			tracer.end(scope);

			return product;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}



}
