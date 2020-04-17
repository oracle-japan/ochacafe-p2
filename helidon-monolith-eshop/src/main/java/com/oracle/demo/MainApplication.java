package com.oracle.demo;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.oracle.demo.cart.CartResource;
import com.oracle.demo.mall.ProductResource;
import com.oracle.demo.order.OrderResource;

import io.helidon.common.CollectionsHelper;

/**
 * Simple JAXRS Application that registers one resource class.
 */
@ApplicationScoped
@ApplicationPath("/")
public class MainApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return CollectionsHelper.setOf(
        		ProductResource.class,
        		CartResource.class,
        		OrderResource.class
        		);
    }
}
