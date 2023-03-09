package org.openlca.proto.io.server;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openlca.core.services.ServerConfig;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Server {

	private final ServerConfig config;
	private final io.grpc.Server server;

	public Server(ServerConfig config) {
		this.config = Objects.requireNonNull(config);
		var db = config.db();
		this.server = ServerBuilder.forPort(config.port())
			.maxInboundMessageSize(1024 * 1024 * 1024)
			.addService(new DataFetchService(db))
			.addService(new DataUpdateService(db))
			.addService(new FlowMapService(db))
			.addService(new ResultService(config))
			.addService(new AboutService(db))
			.build();
	}

	public void start() {
		try {
			var log = LoggerFactory.getLogger(getClass());
			log.info("start server: localhost:{}", config.port());
			server.start();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("shut down server");
				try {
					Server.this.stop();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				System.out.println("server shut down");
			}));
			log.info("server waiting for connections");
			server.awaitTermination();
		} catch (Exception e) {
			throw new RuntimeException("failed to start server", e);
		}
	}

	public synchronized void stop() {
		try {
			if (!server.isShutdown()) {
				server.shutdown().awaitTermination(5, TimeUnit.MINUTES);
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to stop server", e);
		}
	}

	public static void main(String[] args) {

		var log = LoggerFactory.getLogger(Server.class);
		try {
			log.info("parse server configuration");
			var config = ServerConfig.parse(args);
			var server = new Server(config);

			// register a shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {

				// shutdown the server
				try {
					if (!server.server.isShutdown()) {
						log.info("shutdown server");
						server.stop();
					}
				} catch (Exception e) {
					log.error("failed to shutdown server", e);
				}

				// close the database
				try {
					config.db().close();
				} catch (Exception e) {
					log.error("failed to close database");
				}
			}));

			log.info("Start server");
			server.start();
		} catch (Exception e) {
			System.err.println("Server error: " + e.getMessage());
		}
	}
}
