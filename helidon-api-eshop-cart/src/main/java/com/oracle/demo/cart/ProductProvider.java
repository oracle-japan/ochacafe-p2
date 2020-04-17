package com.oracle.demo.cart;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.oracle.demo.cart.client.DelegateProductResource;
import com.oracle.demo.cart.entity.CartEntity;
import com.oracle.demo.cart.entity.CartItemEntity;
import com.oracle.demo.cart.entity.InventoryEntity;
import com.oracle.demo.cart.entity.ProductEntity;
import com.oracle.demo.util.observable.Traceable;

@Dependent
public class ProductProvider {

	@Inject
	@ConfigProperty(name = "app.product.api.baseuri")
	private String baseUri;

	@Deprecated
	protected ProductProvider() {
	}

	@Traceable
	private DelegateProductResource getClient() {
		try {
			RestClientBuilder builder =
					(baseUri != null ? RestClientBuilder.newBuilder().baseUri(new URI(this.baseUri)) : RestClientBuilder.newBuilder());
			return builder.build(DelegateProductResource.class);
		} catch (URISyntaxException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 商品情報を取得する。
	 * @param productId 商品ID
	 * @return 商品のエンティティ
	 */
	public ProductEntity getProduct(String productId) {
		return getClient().get(productId);
	}

	/**
	 * カート情報内の在庫情報を更新する
	 * @param cart 更新するカート情報
	 */
	@Traceable
	public void updateStockCount(CartEntity cart) {
		try {
			ArrayList<String> productList = new ArrayList<String>();
			for (CartItemEntity cartItem : cart.getItems()) {
				productList.add(cartItem.getProductId());
			}

			Collection<InventoryEntity> inventories = getClient().retrieveStockCounts(productList);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (InventoryEntity inventory : inventories) {
				map.put(inventory.getProductId(), inventory.getStockCount());
			}

			for (CartItemEntity cartItem : cart.getItems()) {
				cartItem.updateStockCount(map.get(cartItem.getProductId()));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


}
