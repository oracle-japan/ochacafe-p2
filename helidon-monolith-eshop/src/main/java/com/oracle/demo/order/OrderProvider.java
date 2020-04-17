package com.oracle.demo.order;

import java.util.Collection;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.oracle.demo.order.entity.OrderEntity;
import com.oracle.demo.order.entity.SearchResultEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;

/**
 * 注文情報の取得・操作のビジネスロジックを主役したプロバイダ・クラス
 */
@Dependent
public class OrderProvider {
	@PersistenceContext(unitName = "CartUnit")
	private EntityManager cartEm;

	@PersistenceContext(unitName = "OrderUnit")
	private EntityManager em;

	private CDITraceScope tracer;

	@Inject
	protected OrderProvider(CDITraceScope tracer) {
		this.tracer = tracer;
	}

	/**
	 * 注文情報を取得する
	 * @param userId ユーザID
	 * @param orderId 注文番号
	 * @return 注文情報のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public OrderEntity getOrder(String userId, String orderId) {
		try {
			Scope scope = tracer.begin("get_order");

			OrderEntity order = em.find(OrderEntity.class, orderId);

			if(order == null) {
				String errorMessage =String.format("Resource for order-id \"%s\" not found", orderId);
				tracer.end(scope, errorMessage);
				return null;
			}

			tracer.end(scope);

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 注文情報のステータスを変更する。
	 * @param orderId 注文ID
	 * @param status 変更後のステータス
	 * @return 変更後の注文情報のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public OrderEntity processOrder(String orderId, OrderEntity.Status status) {
		try {
			Scope scope = tracer.begin("process_order");

			OrderEntity order = em.find(OrderEntity.class, orderId);

			if(order == null) {
				String errorMessage =String.format("Resource for order-id \"%s\" not found", orderId);
				tracer.end(scope, errorMessage);
				return null;
			}

			order.setStatus(status);
			em.merge(order);
			em.flush();

			tracer.end(scope);

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 注文情報をリストする
	 * @param userId ユーザID
	 * @param offset 一覧のオフセット
	 * @param limit 取得する上限の件数
	 * @param sort ソートの順序 ("asc"/"desc")
	 * @return 注文情報の一覧のエンティティ
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public SearchResultEntity listOrder(String userId, int offset, int limit, boolean descending) {
		Collection<OrderEntity> results = null;

		try {
			String namedQuery = (descending ? "OrderEntity.All.Descending" : "OrderEntity.All.Ascending");

			Scope scope = tracer.begin("get_order_list");

			TypedQuery<OrderEntity> query = em.createNamedQuery(namedQuery, OrderEntity.class)
					.setFirstResult(offset);
			results = (limit > 0 ? query.setMaxResults(limit) : query).getResultList();

			tracer.end(scope);

			return new SearchResultEntity(results, offset);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}
