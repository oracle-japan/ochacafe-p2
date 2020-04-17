package com.oracle.demo.util.observable;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;

/**
 * Tracerに対してキャリアからspan contextをextractするインタフェース
 */
public interface TraceExtractor {
	/**
	 * 指定したTracerに対してキャリアから span conextをextractする
	 * @param tracer トレーサー
	 */
	public SpanContext extract(Tracer tracer);

	/**
	 * Kafkaレコードにからspan contextをTracerにextractする TraceExtractor
	 *
	 * @param <K> Kafkaレコードのキーのタイプ
	 * @param <V> Kafkaレコードの値のタイプ
	 */
	public class KafkaConsumerRecordCarrier<K, V> implements TraceExtractor {
		private ConsumerRecord<K, V> record;
		private KafkaConsumerRecordCarrier(ConsumerRecord<K, V> record) {
			this.record = record;
		}
		public static <K, V> KafkaConsumerRecordCarrier <K, V> build(ConsumerRecord<K, V> record) {
			return new KafkaConsumerRecordCarrier<K, V>(record);
		}

		@Override
		public SpanContext extract(Tracer tracer) {
			Map<String, String> map = new HashMap<String, String>();
			TextMap textMap = new TextMapAdapter(map);

			for (Header header : this.record.headers()) {
				map.put(header.key(), new String(header.value()));
			}

			return tracer.extract(Format.Builtin.HTTP_HEADERS, textMap);
		}
	}
}
