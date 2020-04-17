package com.oracle.demo.util.observable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import io.helidon.microprofile.tracing.TracerProducer;
import io.opentracing.Span;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;

/**
 * CDIされるオブジェクトのメソッドに対してトレース情報を付与するためのinterceptor
 */
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@Traceable
final class TraceableInterceptor {
	private TracerProducer producer;
	private Tracer tracer;

	@Inject
	public TraceableInterceptor(TracerProducer producer) {
		this.producer = producer;
		this.tracer = this.producer.tracer();
	}

	@AroundInvoke
	public Object obj(InvocationContext ic) throws Exception {
		Object result = null;
		String className = ic.getMethod().getDeclaringClass().getName();
		String methodName = ic.getMethod().getName();
		String operationName = className + "." + methodName; // operationName = <package>.<class>.<method>

		// 呼び出したクラス、メソッドの情報をタグに付与
		SpanBuilder builder = tracer.buildSpan(operationName)
				.asChildOf(tracer.activeSpan())
				.withTag("span.kind", "app")
				.withTag("app.class", className)
				.withTag("app.method", methodName);
		Span span = null;
		Scope scope = null;

		Object[] params = ic.getParameters();
		for (int i = 0; i < params.length; i++) { // メソッドのパラメータをタグに付与
			String key = "app.arg_" + Integer.toString(i);
			builder.withTag(key, params[i].toString());
		}

		try {
			span = builder.start();
			scope = tracer.scopeManager().activate(span);
			result = ic.proceed();
			span.finish();
			scope.close();
		} catch (Exception ex) { // 例外発生時に例外情報をタグに付与
			span.setTag("error", true);
			span.setTag("app.exception", ex.getClass().getName());
			span.finish();
			scope.close();
			ex.printStackTrace();
			throw ex;
		}

		return result;
	}
}
