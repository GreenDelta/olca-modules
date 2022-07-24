package org.openlca.ipc.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.results.FullResult;
import org.openlca.ipc.Cache;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.ipc.Server;
import org.openlca.jsonld.Json;

import java.util.UUID;
import java.util.function.Function;

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



	@SuppressWarnings("unchecked")
	RpcResponse requireResult(
		RpcRequest req, Function<CachedResult<FullResult>, RpcResponse> handler) {
		var cached = getCachedResultOf(req);
		if (cached.isError())
			return cached.error();
		var value = cached.value();
		return value.result() instanceof FullResult
			? handler.apply((CachedResult<FullResult>) value)
			: Responses.badRequest("the request requires a result object", req);
	}

}
