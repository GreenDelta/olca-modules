package org.openlca.ipc.handlers;

import org.openlca.core.model.CalculationSetup;
import org.openlca.ipc.Responses;
import org.openlca.ipc.RpcRequest;
import org.openlca.jsonld.output.DbRefs;

record CachedResult<T>(CalculationSetup setup, T result, DbRefs refs) {

	public static <T> CachedResult<T> of(
		HandlerContext context, CalculationSetup setup, T result) {
		var refs = DbRefs.of(context.db());
		return new CachedResult<T>(setup, result, refs);
	}

	<R> Effect<R> require(Class<R> clazz, RpcRequest req) {
		if (!clazz.isInstance(result))
			return Effect.error(
				Responses.badRequest(
					"method call requires a result type of " + clazz, req));
		return Effect.ok(clazz.cast(result));
	}

}
