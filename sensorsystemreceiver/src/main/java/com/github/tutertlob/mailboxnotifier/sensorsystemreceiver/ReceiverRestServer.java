package com.github.tutertlob.mailboxnotifier.sensorsystemreceiver;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class ReceiverRestServer {

	private static final Logger logger = Logger.getLogger(ReceiverRestServer.class.getName());

	private static HttpServer server;

	private static String base_uri;

	private static String getBaseUri() {
		String host;
		try {
			host = AppProperties.getProperty("rest.host");
		} catch (IllegalArgumentException | NullPointerException e) {
			host = "localhost";
		}

		String port;
		try {
			port = AppProperties.getProperty("rest.port");
		} catch (IllegalArgumentException | NullPointerException e) {
			port = "49152";
		}

		return String.format("http://%s:%s/receiver", host, port);
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	private static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and providers
		// in site.tutertlob.mailboxnotifier.sensorsystemreceiver package
		final ResourceConfig rc = new ResourceConfig()
				.packages("com.github.tutertlob.mailboxnotifier.sensorsystemreceiver");

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(base_uri), rc);
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void startRestServer() throws IOException {
		logger.info("Starting ReceiverRestServer...");
		base_uri = getBaseUri();
		server = startServer();
	}

	public static void finish() {
		logger.info("Stopping ReceiverRestServer...");
		server.stop();
	}

}
