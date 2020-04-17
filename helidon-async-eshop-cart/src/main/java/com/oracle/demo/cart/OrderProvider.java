package com.oracle.demo.cart;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.oracle.demo.cart.entity.CartEntity;
import com.oracle.demo.cart.entity.CartItemEntity;
import com.oracle.demo.cart.entity.OrderEntity;
import com.oracle.demo.cart.entity.OrderItemEntity;
import com.oracle.demo.util.observable.CDITraceScope;
import com.oracle.demo.util.observable.TraceInjector;
import com.oracle.demo.util.observable.Traceable;
import com.oracle.demo.util.streaming.KafkaProvider;

/**
 * 注文処理を行うビジネスロジックを集約したプロバイダ・クラス
 * Kafkaに対する注文のエントリを行う
 */
@Dependent
public class OrderProvider {
	@Inject
	@ConfigProperty(name = "app.order.stream.name")
	private String streamName;

	private CDITraceScope tracer;
	private KafkaProvider kafkaProvider;

	@Inject
	protected OrderProvider(CDITraceScope tracer, KafkaProvider kafkaProvider) {
		this.kafkaProvider = kafkaProvider;
		this.tracer = tracer;
	}

	private JsonObject toOrderJson(OrderEntity order) {
		JsonArrayBuilder jab = Json.createArrayBuilder();
		for(OrderItemEntity orderItem : order.getItems()) {
			jab.add(Json.createObjectBuilder()
					.add("product_id", orderItem.getProductId())
					.add("count", orderItem.getCount())
					.build());
		}

		return Json.createObjectBuilder()
				.add("id", order.getId())
				.add("user_id", order.getUserId())
				.add("order_date", order.getOrderDate().toString())
				.add("items", jab)
				.build();
	}

	/**
	 * チェックアウトを行う。
	 * Kafkaに対して注文エントリのメッセージをProduceする
	 * @param cart チェックアウトするカート情報のエンティティ
	 * @return 注文情報のエンティティ
	 */
	@Traceable
	public OrderEntity checkout(CartEntity cart) {
		OrderEntity order = OrderEntity.newInstance(cart.getUserId());

		for (CartItemEntity cartItem : cart.getItems()) {
			OrderItemEntity orderItem = OrderItemEntity.newInstance(
					order,
					cartItem.getProductId(),
					cartItem.getProductName(),
					cartItem.getCount(),
					cartItem.getPrice());
			order.getItems().add(orderItem);
		}

		JsonObject json = toOrderJson(order);

		// ここからKafkaにポスト
		Producer<String, String> producer = kafkaProvider.producer();
		ProducerRecord<String, String> record = new ProducerRecord<String, String>(streamName, order.getId(), json.toString());

		// Kafka ヘッダにトレース情報をinject
		tracer.inject(TraceInjector.KafkaProducerRecordCarrier.build(record));

		producer.send(record);
		producer.close();

		return order;
	}
}
