package com.oracle.demo.util.streaming;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@ApplicationScoped
public class KafkaProvider {
	public static final int KAFKA_DEFAULT_AUTO_COMMIT_INTERVAL = 1000; // 1000 msecs

	private Properties consumerProperties = null;
	private Properties producerProperties = null;

	private KafkaProvider() {
		loadConfig();
	}

	private void loadConfig() {
		Config config = ConfigProvider.getConfig();
		Properties baseProperties = new Properties();

		baseProperties.setProperty("bootstrap.servers", config.getValue("kafka.connection.bootstrap-servers", String.class));
		baseProperties.setProperty("security.protocol", config.getValue("kafka.connection.security-protocol", String.class));
		baseProperties.setProperty("sasl.mechanism", config.getValue("kafka.connection.sasl-mechanism", String.class));
		baseProperties.setProperty("sasl.jaas.config", config.getValue("kafka.connection.sasl-jaas-config", String.class));
		baseProperties.setProperty("retries", config.getValue("kafka.config.retries", String.class));
		baseProperties.setProperty("max.request.size", config.getValue("kafka.config.max-request-size", String.class));

		this.consumerProperties = (Properties) baseProperties.clone();
		this.consumerProperties.setProperty("key.deserializer", config.getValue("kafka.deserializer.key-deserializer", String.class));
		this.consumerProperties.setProperty("value.deserializer", config.getValue("kafka.deserializer.value-deserializer", String.class));

		this.producerProperties = (Properties) baseProperties.clone();
		this.producerProperties.setProperty("key.serializer", config.getValue("kafka.serializer.key-serializer", String.class));
		this.producerProperties.setProperty("value.serializer", config.getValue("kafka.serializer.value-serializer", String.class));
	}

	public static final KafkaProvider buildProvider() {
		return new KafkaProvider();
	}

	public <K, V> Producer<K, V> producer() {
		return new KafkaProducer<K, V>(this.producerProperties);
	}

	public <K, V> Consumer<K, V> consumer(String groupId, boolean autoCommit) {
		return consumer(groupId, autoCommit, KAFKA_DEFAULT_AUTO_COMMIT_INTERVAL);
	}

	public <K, V> Consumer<K, V> consumer(String groupId, boolean autoCommit, long autoCommitIntervalMillil) {
		Properties props = (Properties) this.consumerProperties.clone();
    	props.setProperty("group.id", groupId);
	    props.setProperty("enable.auto.commit", Boolean.toString(autoCommit));

	    if (autoCommit == true) {
	    	props.setProperty("auto.commit.interval.ms", Long.toString(autoCommitIntervalMillil));
	    }

		return new KafkaConsumer<K, V>(props);
	}

}
