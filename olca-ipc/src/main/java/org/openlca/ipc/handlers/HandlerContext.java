package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.ipc.Cache;
import org.openlca.ipc.Server;

import java.util.UUID;

public record HandlerContext(
	Server server,
	IDatabase db,
	MatrixSolver solver,
	Cache cache) {

	public Object getCached(String id) {
		return cache.get(id);
	}

	public <T> T getCached(Class<T> clazz, String id) {
		var obj = cache.get(id);
		return clazz.isInstance(obj)
			? clazz.cast(obj)
			: null;
	}

	public String cache(Object object) {
		var id = UUID.randomUUID().toString();
		cache.put(id, object);
		return id;
	}

	public Object popCached(String id) {
		return cache.remove(id);
	}
}
