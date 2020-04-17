package com.oracle.demo.util.observable;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;

@ApplicationScoped
abstract class AbstractTraceScope implements TraceScope {

	@Inject
	protected AbstractTraceScope() {
	}

	protected abstract Tracer tracer();

	@Override
	public final Scope begin() {
		return begin(Thread.currentThread().getStackTrace()[2], null, null); // 呼出し元のコードのスタックトレース・エントリを元に生成
	}

	@Override
	public final Scope begin(String operationName) {
		return begin(Thread.currentThread().getStackTrace()[2], operationName, null); // 呼出し元のコードのスタックトレース・エントリを元に生成
	}

	public final Scope begin(String operationName, TraceExtractor extractor) {
		return begin(Thread.currentThread().getStackTrace()[2], operationName, extractor); // 呼出し元のコードのスタックトレース・エントリを元に生成
	}

	private final Scope begin(StackTraceElement ste, String operationName, TraceExtractor extractor) {
		String className = ste.getClassName();
		String methodName = ste.getMethodName();
		int beginLineNo = ste.getLineNumber();

		operationName = (operationName == null ? String.format("%s.%s:%d", className, methodName, beginLineNo) : operationName);

		Tracer tracer = tracer();
		Span activeSpan = tracer.activeSpan();
		activeSpan = (activeSpan != null ? activeSpan : tracer.buildSpan(operationName).start());
		SpanContext parentContext = (extractor != null ? extractor.extract(tracer): activeSpan.context());

		SpanBuilder builder = tracer.buildSpan(operationName)
				.asChildOf(parentContext)
				.withTag("span.kind", "app")
				.withTag("app.class", className)
				.withTag("app.method", methodName)
				.withTag("app.begin_line", ste.getLineNumber());

		// Start span
		Span span = builder.start();

		return tracer.scopeManager().activate(span);
	}

	@Override
	public final void end(Scope scope, String errorMessage) {
		if (scope != null) {
			int endLineNo = Thread.currentThread().getStackTrace()[2].getLineNumber();
			Span span = this.tracer().activeSpan();
			span.setTag("app.end_line", endLineNo);
			if (errorMessage != null) {
				span.setTag("error", true);
				span.setTag("error_message", errorMessage);
				Map<String, String> map = new HashMap<String, String>();
				map.put("event", "error");
				span.log(map);
			}
			span.finish();
			scope.close();
		}
	}

	@Override
	public final void end(Scope scope) {
		if (scope != null) {
			int endLineNo = Thread.currentThread().getStackTrace()[2].getLineNumber();
			Span span = this.tracer().activeSpan();
			span.setTag("app.end_line", endLineNo);
			span.finish();
			scope.close();
		}
	}

	@Override
	public void inject(TraceInjector injector) {
		injector.inject(this.tracer());
	}

}
