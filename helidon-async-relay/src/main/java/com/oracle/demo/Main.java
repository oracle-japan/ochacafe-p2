package com.oracle.demo;

import java.io.IOException;

import com.oracle.demo.messaging.MessageRelay;

/**
 * The application main class.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() { }

    /**
     * Application main entry point.
     * @param args command line arguments.
     * @throws IOException if there are problems reading logging properties
     */
    public static void main(final String[] args) throws IOException {
    	MessageRelay relay = MessageRelay.builder().build();

    	relay.start();
    }

}
