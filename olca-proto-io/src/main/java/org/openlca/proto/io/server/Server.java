package org.openlca.proto.io.server;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.openlca.core.DataDir;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.nativelib.NativeLib;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Server {

	private final int port;
	private final io.grpc.Server server;

	public Server(IDatabase db, int port) {
		this.port = port;
		this.server = ServerBuilder.forPort(port)
			.maxInboundMessageSize(1024 * 1024 * 1024)
			.addService(new DataFetchService(db))
			.addService(new DataUpdateService(db))
			.addService(new FlowMapService(db))
			.addService(new ResultService(db))
			.addService(new AboutService(db))
			.build();
	}

	public void start() {
		try {
			var log = LoggerFactory.getLogger(getClass());
			log.info("start server: localhost:{}", port);
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

	public void stop() {
		if (server == null)
			return;
		try {
			server.shutdown().awaitTermination(5, TimeUnit.MINUTES);
		} catch (Exception e) {
			throw new RuntimeException("failed to stop server", e);
		}
	}

	public static void main(String[] args) {

		String dbArg = null;
		String portArg = null;
		String nativeArg = null;

		String flag = null;
		for (var arg : args) {
			if (arg.startsWith("-")) {
				flag = arg;
				continue;
			}
			if (flag == null) {
				System.err.println("Invalid argument: " + arg);
				return;
			}
			switch (flag) {
				case "-db" -> dbArg = arg;
				case "-port" -> portArg = arg;
				case "-native" -> nativeArg = arg;
				default -> {
					System.err.println("Unknown flag: " + flag);
					return;
				}
			}
		}

		int port;
		if (portArg == null) {
			System.out.println("No port given. Take 8080 as default");
			port = 8080;
		} else {
			try {
				port = Integer.parseInt(portArg, 10);
			} catch (Exception e) {
				System.err.println(portArg + " is not a valid port number.");
				return;
			}
		}

		try {

			// try to load the native libraries
			if (nativeArg == null) {
				var deaultDir = DataDir.get().root();
				System.out.println("Load native libraries from " + deaultDir);
				NativeLib.loadFrom(deaultDir);
			} else {
				var nativeLibDir = new File(nativeArg);
				if (!nativeLibDir.exists() || !nativeLibDir.isDirectory()) {
					System.err.println(nativeLibDir.getAbsolutePath()
						+ " is not a directory");
					System.exit(-1);
				}
				System.out.println("Load native libraries from "
					+ nativeLibDir.getAbsolutePath());
				NativeLib.loadFrom(nativeLibDir);
			}

			// try to open the database
			IDatabase db;
			if (dbArg == null) {
				var defaultDb = new File("database");
				System.out.println("Open default database " + defaultDb);
				db = new Derby(defaultDb);
			} else {
				var dataDbDir = DataDir.get().getDatabaseDir(dbArg);
				if (dataDbDir.exists()) {
					System.out.println("Open database " + dataDbDir);
					db = new Derby(dataDbDir);
				} else {
					var dbDir = new File(dbArg);
					System.out.println("Open database " + dbDir);
					db = new Derby(dbDir);
				}
			}

			// check if the database needs an update
			if (db.getVersion() < IDatabase.CURRENT_VERSION) {
				Upgrades.on(db);
			}

			System.out.println("Start server");
			new Server(db, port).start();
			System.out.println("close database...");
			db.close();
			System.out.println("database closed.");
		} catch (Exception e) {
			System.err.println("Server error: " + e.getMessage());
		}
	}
}
