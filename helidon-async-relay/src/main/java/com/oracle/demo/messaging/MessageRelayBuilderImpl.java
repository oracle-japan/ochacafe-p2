package com.oracle.demo.messaging;

import com.oracle.demo.messaging.MessageRelay.Builder;

import io.helidon.config.Config;

class MessageRelayBuilderImpl implements MessageRelay.Builder {
	private String topicName = null;
	private String targetUrl = null;
	private String groupId = null;
	private long pollTimeout = 10000;	// default
	private boolean autoCommit = false;	// default
	private long autoCommitIntervalMillis = 100;	//default

	private Config relayConfig = null;

	public MessageRelayBuilderImpl() {
		loadRelayConfig();
	}

	private void loadRelayConfig() {
		this.relayConfig = Config.create().get("relay");
		this.topicName = this.relayConfig.get("topic-name").asString().get();
		this.groupId = this.relayConfig.get("group-id").asString().get();
		this.pollTimeout = this.relayConfig.get("poll-timeout").asLong().get();
		this.targetUrl = this.relayConfig.get("target").get("url").asString().get();
	}

	@Override
	public MessageRelay build() {
		if (this.topicName == null) {
			throw new MessageRelayException("Topic name is null.", null);
		}
		if (this.targetUrl == null ) {
			throw new MessageRelayException("Target URL name is null.", null);
		}

		return new MessageRelayImpl(this.topicName, this.targetUrl, this.groupId, this.pollTimeout, this.autoCommit, this.autoCommitIntervalMillis);
	}

	@Override
	public MessageRelay build(String topicName) {
		topic(topicName);
		return build();
	}

	@Override
	public Builder topic(String topicName) {
		this.topicName = topicName;
		return this;
	}

	@Override
	public Builder target(String targetUrl) {
		this.targetUrl = targetUrl;
		return this;
	}

	@Override
	public Builder pollTimeout(long timeout) {
		this.pollTimeout = timeout;
		return this;
	}

	@Override
	public Builder group(String groupId) {
		this.groupId = groupId;
		return this;
	}

	@Override
	public Builder autoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
		return this;
	}

	@Override
	public Builder autoCommitInterval(long millis) {
		this.autoCommitIntervalMillis = millis;
		return this;
	}


}
