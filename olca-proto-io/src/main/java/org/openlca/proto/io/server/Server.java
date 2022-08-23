package org.openlca.proto.io.server;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.nativelib.NativeLib;
import org.slf4j.LoggerFactory;

import io.grpc.ServerBuilder;

public class Server {

	private final int port;
	private final io.grpc.Server server;
	private final IDatabase db;

	public Server(IDatabase db, DataDir dataDir, int port) {
		this.port = port;
		this.db = Objects.requireNonNull(db);
		this.server = ServerBuilder.forPort(port)
			.maxInboundMessageSize(1024 * 1024 * 1024)
			.addService(new DataFetchService(db))
			.addService(new DataUpdateService(db))
			.addService(new FlowMapService(db))
			.addService(new ResultService(db, dataDir.getLibraryDir()))
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

	public synchronized void stop() {
		try {
			if (!server.isShutdown()) {
				server.shutdown().awaitTermination(5, TimeUnit.MINUTES);
			}
			db.close();
		} catch (Exception e) {
			throw new RuntimeException("failed to stop server", e);
		}
	}

	public static void main(String[] args) {

		// read program arguments
		String dbArg = null;
		String portArg = null;
		String dataArg = null;
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
				case "-data" -> dataArg = arg;
				case "-db" -> dbArg = arg;
				case "-native" -> nativeArg = arg;
				case "-port" -> portArg = arg;
				default -> {
					System.err.println("Unknown flag: " + flag);
					return;
				}
			}
		}

		// port -> default = 8080
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

		// data dir
		var dataDir = dataArg != null
			? DataDir.get(new File(dataArg))
			: DataDir.get();

		try {

			// try to load the native libraries
			var nativeDir = nativeArg != null
				? new File(nativeArg)
				: dataDir.root();
			System.out.println("Load native libraries from " + nativeDir);
			NativeLib.loadFrom(dataDir.root());
			if (!NativeLib.isLoaded()) {
				System.out.println("... could not load native libraries");
			}

			// try to open the database
			var dbName = dbArg != null
				? dbArg
				: "database";
			System.out.println("Open database " + dbName);
			var db = dataDir.openDatabase(dbName);

			// check if the database needs an update
			if (db.getVersion() < IDatabase.CURRENT_VERSION) {
				Upgrades.on(db);
			}

			System.out.println("Start server");
			new Server(db, dataDir, port).start();
		} catch (Exception e) {
			System.err.println("Server error: " + e.getMessage());
		}
	}
}
