package org.openlca.ipc;

import java.io.File;
import java.io.IOException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private String db;
	private String port;

	private static Main parseArgs(String[] args) {
		Main main = new Main();
		if (args == null)
			return main;
		String flag = null;
		for (String arg : args) {
			if (flag == null && arg.startsWith("-")) {
				flag = arg.trim().toLowerCase();
				continue;
			}
			if (flag == null)
				continue;
			switch (flag) {
			case "-db":
				main.db = arg;
				break;
			case "-port":
				main.port = arg;
				break;
			}
			flag = null;
		}
		return main;
	}

	private void startServer() {
		IDatabase db = initDB();
		if (db == null)
			return;
		int port = initPort();
		try {
			// TODO: native config
			Server server = new Server(port)
				.withDefaultHandlers(db, new JavaSolver());
			server.start();
			Runtime.getRuntime().addShutdownHook(
				new Thread(() -> shutdown(server, db)));
		} catch (Exception e) {
			log.error("Failed to start server", e);
		}
	}

	private IDatabase initDB() {
		String dbDir = this.db;
		if (dbDir == null) {
			log.info("No database given; use default database folder `db`");
			dbDir = "db";
		}
		try {
			return new DerbyDatabase(new File(dbDir));
		} catch (Exception e) {
			log.error("Could not initialize database", e);
			return null;
		}
	}

	private int initPort() {
		int port = -1;
		if (this.port != null) {
			try {
				port = Integer.parseInt(this.port);
			} catch (Exception e) {
				log.error(this.port + " is not a valid port number", e);
			}
		}
		if (port < 0) {
			port = 0;
			log.info("Start the server on a random port");
		}
		return port;
	}

	private void shutdown(Server server, IDatabase db) {
		try {
			log.info("Shutdown server");
			server.stop();
			db.close();
			log.info("all done");
		} catch (Exception e) {
			log.error("Failed to shutdown server gracefully", e);
		}
	}

	public static void main(String[] args) throws IOException {
		parseArgs(args).startServer();
	}

}
