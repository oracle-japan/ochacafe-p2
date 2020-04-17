package com.oracle.demo.util.observable;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;

import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapAdapter;

/**
 * Tracerからactive span contextをキャリアにinjectするインタフェース
 */
public interface TraceInjector {
	/**
	 * 指定したTracerからactive span conextをinjectする
	 * @param tracer トレーサー
	 */
	public void inject(Tracer tracer);

	/**
	 * Kafkaレコードに対してactive span contextをinjectする TraceInjector
	 *
	 * @param <K> Kafkaレコードのキーのタイプ
	 * @param <V> Kafkaレコードの値のタイプ
	 */
	public class KafkaProducerRecordCarrier<K, V> implements TraceInjector {
		private ProducerRecord<K, V> record;

		private KafkaProducerRecordCarrier(ProducerRecord<K, V> record) {
			this.record = record;
		}
		public static <K, V> KafkaProducerRecordCarrier <K, V> build(ProducerRecord<K, V> record) {
			return new KafkaProducerRecordCarrier<K, V>(record);
		}

		@Override
		public void inject(Tracer tracer) {
			Map<String, String> map = new HashMap<String, String>();
			TextMap textMap = new TextMapAdapter(map);

			tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, textMap);
			Headers kafkaHeaders = record.headers();

			for (String key : map.keySet()) {
				kafkaHeaders.add(key, map.get(key).getBytes());
			}
		}
	}

}
