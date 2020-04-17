package com.oracle.demo.cart;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.oracle.demo.cart.client.DelegateOrderResource;
import com.oracle.demo.cart.entity.OrderEntity;

@Dependent
public class OrderProvider {

	@Inject
	@ConfigProperty(name = "app.order.api.baseuri")
	private String baseUri;

	@Deprecated
	protected OrderProvider() {
	}

	private DelegateOrderResource getClient() {
		try {
			RestClientBuilder builder =
					(baseUri != null ? RestClientBuilder.newBuilder().baseUri(new URI(this.baseUri)) : RestClientBuilder.newBuilder());
			return builder.build(DelegateOrderResource.class);
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 商品を注文する。
	 * @param userId 注文するユーザのユーザID
	 * @param orderList 商品IDと個数の一覧を示すマップ
	 * @return
	 */
	public OrderEntity checkout(String userId, Map<String, Integer> orderList) {
		return getClient().checkout(userId, orderList);
	}

}
