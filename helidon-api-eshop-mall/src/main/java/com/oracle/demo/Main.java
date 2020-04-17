package com.oracle.demo;

import io.helidon.microprofile.server.Server;

import java.io.IOException;

public final class Main {

	private Main() {} // To block non-args constructor invocation.

	private static final Server startServer() {
		return Server.create().start();
	}

	public static void main(final String[] args) throws IOException {
		Server server = startServer();
		System.out.println("http://localhost:" + server.port());
	}

}
