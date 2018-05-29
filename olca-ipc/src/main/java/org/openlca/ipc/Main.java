package org.openlca.ipc;

import java.io.File;
import java.io.IOException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.matrix.solvers.JavaSolver;

public class Main {

	public static void main(String[] args) throws IOException {
		if (args == null || args.length == 0 || args[0] == null || args[0].trim().isEmpty())
			throw new IllegalArgumentException("Missing database path as argument");
		File dir = new File(args[0]);
		if (!dir.exists() || !dir.isDirectory())
			throw new IllegalArgumentException("Database directory does not exist");
		int port = 8080;
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
		IDatabase db = new DerbyDatabase(dir);
		new Server(port)
				.withDefaultHandlers(db, new JavaSolver())
				.start(); // TODO: native config
	}

}