package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.ipc.Cache;
import org.openlca.ipc.Server;

public class HandlerContext {

	public final Server server;
	public final IDatabase db;
	public final Cache cache;
	public final MatrixSolver solver;

	public HandlerContext(
		Server server,
		IDatabase db,
		MatrixSolver solver,
		Cache cache) {
		this.server = server;
		this.db = db;
		this.solver = solver;
		this.cache = cache;
	}

}
