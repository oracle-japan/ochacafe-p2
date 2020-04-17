package com.oracle.demo.messaging;


import com.oracle.demo.util.observable.TraceExtractor;
import com.oracle.demo.util.observable.TraceScope;
import com.oracle.demo.util.streaming.KafkaProvider;

import io.opentracing.Scope;

import java.io.StringReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

class MessageRelayImpl implements MessageRelay {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

    private static final JsonReaderFactory JSON_READER = Json.createReaderFactory(Collections.emptyMap());

    private TraceScope tracer;

	private Consumer<String, String> consumer = null;
	private String topicName = null;
	private String groupId = null;
	private long pollTimeout = 0L;
	private boolean autoCommit = false;
	private long autoCommitIntervalMillis = KafkaProvider.KAFKA_DEFAULT_AUTO_COMMIT_INTERVAL;

	private Client restClient = null;
	private WebTarget webTarget = null;
	private String targetUrl = null;

	private boolean isActive = false;

	public MessageRelayImpl(
			String topicName,
			String targetUrl,
			String groupId,
			long pollTimeout,
			boolean autoCommit, long autoCommitIntervalMillis) {

		this.topicName = topicName;
		this.targetUrl = targetUrl;
		this.groupId = groupId;
		this.pollTimeout = pollTimeout;
		this.autoCommit = autoCommit;

		if(autoCommit) {
			this.autoCommitIntervalMillis = autoCommitIntervalMillis;
		}

		setupTracer();
		setupConsumer();
		setupRestClient();

		this.isActive = true;
	}

	private void setupTracer() {
		this.tracer = TraceScope.build();
	}
	private void setupConsumer() {
		this.consumer = KafkaProvider.buildProvider().consumer(this.groupId, this.autoCommit, this.autoCommitIntervalMillis);
	}

	private void setupRestClient() {
		this.restClient = ClientBuilder.newClient();
		this.webTarget = this.restClient.target(this.targetUrl);
	}

	@Override
	public void start() {
		Scope rootScope = tracer.begin("start_server");

		List<String> topicList = new ArrayList<String>();
		topicList.add(this.topicName);

		try {
			ConsumerRecords<String, String> records = null;

			Scope scope = tracer.begin("subscribe_kafka_topic");

			consumer.subscribe(topicList);

			tracer.end(scope);

			while (isActive) {
				try {
					scope = tracer.begin("poll_kafka_topic");

					logger.log(Level.INFO, "*** Poll topic. ***");

					records = consumer.poll(Duration.ofMillis(this.pollTimeout));

					tracer.end(scope);

					if (records.count() > 0) {
						JsonArrayBuilder jab = Json.createArrayBuilder();
						ConsumerRecord<String, String> lastRecord = null;
						for (ConsumerRecord<String, String> record : records.records(this.topicName)) {
							lastRecord = record;
							Scope asyncScope = tracer.begin("consume_kafka_topic", TraceExtractor.KafkaConsumerRecordCarrier.build(record));

							JsonValue jsonValue = JSON_READER.createReader(new StringReader(record.value())).readValue();
							if (jsonValue.getValueType() == JsonValue.ValueType.ARRAY) {
								JsonArray jsonArray = (JsonArray) jsonValue;
								for (JsonValue value : jsonArray) {
									jab.add(value);
								}
							} else {
								jab.add(jsonValue);
							}

							tracer.end(asyncScope);
						}

						scope = tracer.begin("relay_message", TraceExtractor.KafkaConsumerRecordCarrier.build(lastRecord));

						Entity<JsonValue> entity = Entity.entity(
									jab.build(),
									MediaType.APPLICATION_JSON);
						Response response = this.webTarget.request().buildPost(entity).invoke();

						logger.log(Level.INFO, String.format("Status Code: %s", response.getStatus()));

						if (Response.Status.Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
							consumer.commitSync();
							tracer.end(scope);
						} else {
							tracer.end(scope, String.format("Invalid Status Code: %s", response.getStatus()));
						}


					} else {
						logger.log(Level.INFO, "*** No messages ***");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		tracer.end(rootScope);
	}
}
