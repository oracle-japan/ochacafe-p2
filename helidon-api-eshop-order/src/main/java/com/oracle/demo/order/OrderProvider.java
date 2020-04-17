package com.oracle.demo.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.oracle.demo.order.entity.OrderEntity;
import com.oracle.demo.order.entity.SearchResultEntity;
import com.oracle.demo.product.entity.ProductEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;

/**
 * 注文のビジネスロジックを集約したプロバイダ・クラス
 */
@Dependent
public class OrderProvider {
	@PersistenceContext(unitName = "OrderUnit")
	private EntityManager em;

	@PersistenceContext(unitName = "ProductUnit")
	private EntityManager productEm;

	private CDITraceScope tracer;

	@Inject
	protected OrderProvider(CDITraceScope tracer) {
		this.tracer = tracer;
	}

	/**
	 * 注文情報を取得する
	 * @param userId ユーザID
	 * @param orderId 注文番号
	 * @return 注文情報
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
	 * @param status 変更後のステータス("ordered"/"cancelled"/"shipped"/"delivered"/"suspended"/"unknown")。それ以外の文字列は "unknown" と等価。
	 * @return 変更後の注文情報
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
	 * @param descending 降順ソートの場合に true。
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




	/**
	 * 注文リストから注文を受け付ける。
	 * @param orderList 注文リスト商品IDと個数のマップ。
	 * @return 受け付けられた注文の商品IDのリスト。
	 */
	@Traceable
	@Transactional(Transactional.TxType.REQUIRED)
	public OrderEntity checkout(String userId, Map<String, Integer> orderList) {
		try {
			ArrayList<String> ordered = new ArrayList<String>();

			Scope scope = tracer.begin("checkout_order");

			if (orderList.size() == 0) {
				String errorMessage = String.format("Order List for user \"%s\" is empty", userId);
				tracer.end(scope, errorMessage);
				return null;
			}

			OrderEntity order = OrderEntity.newInstance(userId);

			for (String productId : orderList.keySet()) {
				ProductEntity product = productEm.find(ProductEntity.class, productId);
				if (product != null) {
					int count = orderList.get(productId);
					if (count > 0 && product.getStockCount() >= count) {
						order.addItem(product.getId(), product.getName(), count, product.getPrice());
						product.setStockCount(product.getStockCount() - count);
						ordered.add(productId);
						productEm.merge(product);
					}
				}
			}

			productEm.flush();

			em.persist(order);
			em.flush();
			em.refresh(order);

			order = em.find(OrderEntity.class, order.getId());

			tracer.end(scope);

			return order;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


}
