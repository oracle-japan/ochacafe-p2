package com.oracle.demo.order;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.oracle.demo.order.entity.OrderEntity;
import com.oracle.demo.order.entity.OrderEntryEntity;
import com.oracle.demo.order.entity.SearchResultEntity;
import com.oracle.demo.util.Converter;

/**
 * 注文(Order)のREST APIを提供するRESTアプリケーションリソース
 */
@Path("/order")
@RequestScoped
@Timed(name = "timer", unit = MetricUnits.MICROSECONDS)
@Counted(name = "counter")
@Metered(name = "meter")
public class OrderResource {
	private OrderProvider provider;

	@Inject
	protected OrderResource(OrderProvider provider) {
		this.provider = provider;
	}

	/**
	 * 注文情報を取得する
	 * @param userId ユーザID
	 * @param orderId 注文番号
	 * @return 注文情報
	 */
	@GET
	@Path("/{user-id}/{order-id}")
    @Produces(MediaType.APPLICATION_JSON)
	public OrderEntity get(@PathParam("user-id") String userId, @PathParam("order-id") String orderId) {
		try {
			OrderEntity order = this.provider.getOrder(userId, orderId);

			if(order == null) {
				String errorMessage =String.format("Resource for order-id \"%s\" not found", orderId);
				throw new NotFoundException(errorMessage);
			}

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
	@PUT
	@Path("/process/{order-id}")
    @Produces(MediaType.APPLICATION_JSON)
	public OrderEntity processOrder(@PathParam("order-id") String orderId, @QueryParam("status") String status) {
		try {
			OrderEntity order = this.provider.processOrder(orderId, OrderEntity.Status.toStatus(status));

			if(order == null) {
				String errorMessage =String.format("Resource for order-id \"%s\" not found", orderId);
				throw new NotFoundException(errorMessage);
			}

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
	 * @return 注文情報の一覧
	 */
	@GET
	@Path("/{user-id}")
    @Produces(MediaType.APPLICATION_JSON)
	public SearchResultEntity listOrder(@PathParam("user-id") String userId,
			@DefaultValue("0") @QueryParam("offset") String offset,
			@DefaultValue("0") @QueryParam("max") String limit,
			@DefaultValue("asc") @QueryParam("sort") String sort) {
		int offsetInt = Converter.toInteger(offset).orElse(Integer.valueOf(0)).intValue();
		int limitInt = Converter.toInteger(limit).orElse(Integer.valueOf(0)).intValue();

		try {
			return this.provider.listOrder(userId, offsetInt, limitInt, sort.equalsIgnoreCase("desc"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 複数の注文リストによる注文を受け付ける。
	 * @param orderList 注文リスト商品IDと個数のマップ。
	 * @return 受け付けられた注文の商品IDのリスト。
	 *
	 */
	@POST
	@Path("/entry")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public JsonObject checkout(OrderEntryEntity [] orderEntries) {
		try {
			this.provider.checkout(orderEntries);

			return Json.createObjectBuilder()
					.add("status", "success")
					.build();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}

