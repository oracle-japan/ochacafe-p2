package com.oracle.demo.mall.client;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.oracle.demo.mall.entity.SearchResultEntity;
import com.oracle.demo.mall.entity.ProductEntity;

/**
 * OrderのREST APIを呼び出して商品情報の取得を行うREST Clientインタフェース
 */
@RegisterRestClient
public interface DelegateProductResource {
	@GET
	@Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
	public SearchResultEntity search(
			@DefaultValue("0") @QueryParam("offset") String offset,
			@DefaultValue("0") @QueryParam("max") String limit,
			@DefaultValue("asc") @QueryParam("sort") String sort);

	@GET
	@Path("/{product-id}")
    @Produces(MediaType.APPLICATION_JSON)
	public ProductEntity get(@PathParam("product-id") String productId);
}
