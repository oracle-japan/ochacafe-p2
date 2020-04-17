package com.oracle.demo.messaging;

public class MessageRelayException extends RuntimeException {
	private static final long serialVersionUID = -8558005291120186836L;

	public MessageRelayException(String message) {
		super(message);
	}
	public MessageRelayException(String message, Throwable cause) {
		super(message, cause);
	}

	public static final MessageRelayException buildException(String detail, Throwable cause) {
		return new MessageRelayException("Build failed: " + detail, cause);
	}
}
