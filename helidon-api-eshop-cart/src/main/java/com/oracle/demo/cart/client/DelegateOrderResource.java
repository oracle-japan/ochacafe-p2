package com.oracle.demo.cart.client;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.oracle.demo.cart.entity.OrderEntity;

/**
 * OrderのREST APIを呼び出してチェックアウトを行うREST Clientインタフェース
 */
@RegisterRestClient
public interface DelegateOrderResource {
	@POST
	@Path("/checkout/{user-id}")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public OrderEntity checkout(@PathParam("user-id") String userId, Map<String, Integer> orderList);
}
