package com.oracle.demo.product;

import java.util.Collection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.oracle.demo.product.entity.InventoryEntity;
import com.oracle.demo.product.entity.ProductEntity;
import com.oracle.demo.product.entity.SearchResultEntity;
import com.oracle.demo.util.Converter;


/**
 * 商品情報のREST APIを提供するRESTアプリケーションリソース
 */
@Path("/product")
@RequestScoped
@Timed(name = "timer", unit = MetricUnits.MICROSECONDS)
@Counted(name = "counter")
@Metered(name = "meter")
public class ProductResource {
	private ProductProvider provider;

	@Inject
	protected ProductResource(ProductProvider provider) {
		this.provider = provider;
	}

	/**
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param order 価格のソート順を昇順("asc") or 降順("desc")で指定。指定が無い場合、無効な文字の場合は価格の昇順でソート。
	 */
	@GET
	@Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public SearchResultEntity searchProducts(
			@DefaultValue("0") @QueryParam("offset") String offset,
			@DefaultValue("0") @QueryParam("max") String limit,
			@DefaultValue("asc") @QueryParam("sort") String sort) {
		int offsetInt = Converter.toInteger(offset).orElse(Integer.valueOf(0)).intValue();
		int limitInt = Converter.toInteger(limit).orElse(Integer.valueOf(0)).intValue();

		try {
			return this.provider.searchProducts(offsetInt, limitInt, sort.equalsIgnoreCase("desc"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 商品情報を取得する
	 * @param productId 商品ID
	 * @return 商品情報のエンティティ
	 */
	@GET
	@Path("/{product-id}")
    @Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public ProductEntity getProduct(@PathParam("product-id") String productId) {
		try {
			ProductEntity product = this.provider.getProduct(productId);

			if(product == null) {
				String errorMessage =String.format("Resource for product-id \"%s\" not found", productId);
				throw new NotFoundException(errorMessage);
			}

			return product;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 商品IDのリストから在庫数のリストを取得する。無効な商品IDの場合は 0 を返却する。
	 * @param productList 取得する商品IDのリスト。
	 * @return 各商品IDに対応する在庫数のリスト。
	 */
	@POST
	@Path("/inventory")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public Collection<InventoryEntity> retrieveStockCounts(Collection<String> productList) {
		try {
			return this.provider.retrieveStockCounts(productList);
		} catch (Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * 製品情報を登録・更新する
	 * @param product 更新する製品情報のオブジェクト
	 * @return
	 */
	@POST
	@Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject updateProducts(ProductEntity[] products) {
		try {
			this.provider.updateProducts(products);

			return Json.createObjectBuilder()
					.add("status", "success")
					.build();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


}
