package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.ipc.Cache;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.Server;
import org.openlca.jsonld.Json;

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

	public Effect<CachedResult<?>> getCachedResultOf(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject() )
			return Effect.error(Responses.invalidParams(req));
		var param = req.params.getAsJsonObject();
		var resultId = Json.getString(param, "resultId");
		if (resultId == null)
			return Effect.error(Responses.invalidParams("resultId is missing", req));
		CachedResult<?> result = getCached(CachedResult.class, resultId);
		return result != null
			? Effect.ok(result)
			: Effect.error(Responses.notFound(
				"no such result exists; id=" + resultId, req));
	}

}
