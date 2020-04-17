package com.oracle.demo.util.observable;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.helidon.microprofile.tracing.TracerProducer;
import io.opentracing.Tracer;

@Dependent
public class CDITraceScope extends AbstractTraceScope implements TraceScope {
	private TracerProducer producer;

	@Inject
	protected CDITraceScope(TracerProducer producer) {
		this.producer = producer;
	}

	protected Tracer tracer() {
		return this.producer.tracer();
	}
}
