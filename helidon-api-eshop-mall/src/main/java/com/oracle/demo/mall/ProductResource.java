package com.oracle.demo.mall;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.oracle.demo.mall.entity.ProductEntity;
import com.oracle.demo.mall.entity.SearchResultEntity;

/**
 * 商品検索(Mall)のREST APIを提供するRESTアプリケーションリソース
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
	 *　商品一覧を取得する
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param order 価格のソート順を昇順("asc") or 降順("desc")で指定。指定が無い場合、無効な文字の場合は価格の昇順でソート。
	 * @return 商品一覧のエンティティ
	 */
	@GET
	@Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
	public SearchResultEntity search(
			@DefaultValue("0") @QueryParam("offset") String offset,
			@DefaultValue("0") @QueryParam("max") String limit,
			@DefaultValue("asc") @QueryParam("sort") String sort) {
		try {
			SearchResultEntity result = this.provider.search(offset, limit, sort);

			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


	/**
	 * 商品情報を取得する
	 * @param productId 商品ID
	 * @return 商品情報
	 */
	@GET
	@Path("/{product-id}")
    @Produces(MediaType.APPLICATION_JSON)
	public ProductEntity get(@PathParam("product-id") String productId) {
		try {

			// order側にRESTで取得
			ProductEntity product = this.provider.getProduct(productId);

			if(product == null) {
				String errorMessage = String.format("Resource for product-id \"%s\" not found", productId);
				throw new NotFoundException(errorMessage);
			}

			return product;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

}
