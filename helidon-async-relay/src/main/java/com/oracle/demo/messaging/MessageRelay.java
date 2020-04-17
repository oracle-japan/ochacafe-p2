package com.oracle.demo.messaging;

public interface MessageRelay {
	public static interface Builder {
		public MessageRelay build();
		public MessageRelay build(String topicName);
		public Builder topic(String topicName);
		public Builder target(String targetUrl);
		public Builder group(String groupId);
		public Builder pollTimeout(long timeout);
		public Builder autoCommit(boolean autoCommit);
		public Builder autoCommitInterval(long millis);
	}

	public void start();

	public static Builder builder() {
		return new MessageRelayBuilderImpl();
	}
}
