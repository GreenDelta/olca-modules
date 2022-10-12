package org.openlca.ipc.handlers;

import org.openlca.core.model.CalculationSetup;
import org.openlca.jsonld.output.JsonRefs;

record CachedResult<T>(CalculationSetup setup, T result, JsonRefs refs) {

	public static <T> CachedResult<T> of(
		HandlerContext context, CalculationSetup setup, T result) {
		var refs = JsonRefs.of(context.db());
		return new CachedResult<>(setup, result, refs);
	}
}
