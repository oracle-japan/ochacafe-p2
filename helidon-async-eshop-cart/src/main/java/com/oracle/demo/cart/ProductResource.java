package com.oracle.demo.cart;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.oracle.demo.cart.entity.ProductEntity;
import com.oracle.demo.util.observable.CDITraceScope;

import io.opentracing.Scope;

/**
 * 商品情報を更新するためのREST APIを提供するRESTアプリケーションリソース
 */
@Path("/product")
@RequestScoped
@Timed(name = "timer", unit = MetricUnits.MICROSECONDS)
@Counted(name = "counter")
@Metered(name = "meter")
public class ProductResource {
	private CDITraceScope tracer;
	private ProductProvider provider;

	@Inject
	protected ProductResource(CDITraceScope tracer,	ProductProvider provider) {
		this.tracer = tracer;
		this.provider = provider;
	}

	/**
	 * 製品情報を登録・更新する
	 * @param product 更新する製品情報のオブジェクト
	 * @return 更新結果のJSONオブジェクト
	 */
	@POST
	@Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject update(ProductEntity[] products) {
		try {
			Scope scope = tracer.begin("create_cart");

			for (ProductEntity product : products) {
				System.out.println(String.format("id: %s, name: %s, price: %d, stock_count: %d",
						product.getId(),
						product.getName(),
						product.getPrice(),
						product.getStockCount()
						));
				this.provider.updateProduct(product);
			}

			tracer.end(scope);

			return Json.createObjectBuilder()
					.add("status", "success")
					.build();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
}