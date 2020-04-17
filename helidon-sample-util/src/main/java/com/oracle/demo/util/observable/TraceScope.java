package com.oracle.demo.util.observable;

import io.helidon.config.Config;
import io.helidon.tracing.TracerBuilder;
import io.opentracing.Scope;
import io.opentracing.Tracer;

public interface TraceScope {
	/**
	 * デフォルトのoperationName(=<package>.<class>.<method>)でSpanを開始する。
	 * @return 開始されたSpanを制御するためのScope
	 */
	public Scope begin();

	/**
	 * 指定したのoperationNameでSpanを開始する。
	 * @param operationName Spanのoperation name
	 * @return 開始されたSpanを制御するためのScope
	 */
	public Scope begin(String operationName);

	/**
	 * 指定したのoperationNameとExtractorを指定してでSpanを開始する。
	 * @param operationName Spanのoperation name
	 * @param extractor span contextを保持するキャリアから生成されたTraceExtractor
	 * @return
	 */
	public Scope begin(String operationName, TraceExtractor extractor);

	/**
	 * Spanを正常終了する、
	 * @param scope Spanを制御するためのScope
	 */
	public void end(Scope scope);

	/**
	 * Spanをエラー終了する、
	 * @param scope Spanを制御するためのScope
	 * @param error エラーメッセージ
	 */
	public void end(Scope scope, String error);

	/**
	 * TraceInjectorに対してTracerからSpanContextをinjectする
	 * @param injector SpanContextをinjectするキャリアを保持するTraceInjector
	 */
	public void inject(TraceInjector injector);

	/**
	 * Implementation for non-CDI environment
	 * Produce tracer from config named with "tracing." prefix.
	 */
	public static TraceScope build() {
		return new AbstractTraceScope() {
			private Tracer tracer;
			@Override
			protected Tracer tracer() {
				if (this.tracer == null) {
					this.tracer = TracerBuilder.create(Config.create().get("tracing"))
							.registerGlobal(true)
							.build();
				}
				return this.tracer;
			}
		};
	};
}
