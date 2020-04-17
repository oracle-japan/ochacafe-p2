package com.oracle.demo.cart.client;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.oracle.demo.cart.entity.ProductEntity;
import com.oracle.demo.cart.entity.InventoryEntity;

/**
 * OrderのREST APIを呼び出して商品情報の取得、在庫情報の取得を行ううREST Clientインタフェース
 */
@RegisterRestClient
public interface DelegateProductResource {
	@GET
	@Path("/{product-id}")
    @Produces(MediaType.APPLICATION_JSON)
	public ProductEntity get(@PathParam("product-id") String productId);

	@POST
	@Path("/inventory")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Collection<InventoryEntity> retrieveStockCounts(Collection<String> productList);
}
