package com.oracle.demo.mall;

import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.oracle.demo.mall.client.DelegateProductResource;
import com.oracle.demo.mall.entity.ProductEntity;
import com.oracle.demo.mall.entity.SearchResultEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.Traceable;

import io.opentracing.Scope;

/**
 * 商品検索・情報取得のビジネスロジックを集約するプロバイダ・クラス
 */
@Dependent
public class ProductProvider {
	@Inject
	@ConfigProperty(name = "app.product.api.baseuri")
	private String baseUri;

	private CDITraceScope tracer;

	@Inject
	protected ProductProvider(CDITraceScope tracer) {
		this.tracer = tracer;
	}

	/**
	 *　商品一覧を取得する
	 * @param offset 結果を取得するオフセット値。デフォルトでは 0。
	 * @param limit 最大取得件数。デフォルトでは0(無制限)。
	 * @param sort 価格のソート順を昇順("asc") or 降順("desc")で指定。指定が無い場合、無効な文字の場合は価格の昇順でソート。
	 * @return 商品一覧のエンティティ
	 */
	@Traceable
	public SearchResultEntity search(String offset, String limit, String sort) {
		try {
			Scope scope = tracer.begin("search_products");

			SearchResultEntity result = getClient().search(offset, limit, sort);

			tracer.end(scope);

			return result;
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
	@Traceable
	public ProductEntity getProduct(String productId) {
		try {
			Scope scope = tracer.begin("get_product");

			// order側にRESTで取得
			ProductEntity product = getClient().get(productId);

			if(product == null) {
				String errorMessage = String.format("Resource for product-id \"%s\" not found", productId);
				tracer.end(scope, errorMessage);
				return null;
			}

			tracer.end(scope);

			return product;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}


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
}
