package org.openlca.ipc.handlers;

import org.openlca.core.model.CalculationSetup;
import org.openlca.jsonld.output.DbRefs;

record CachedResult<T>(CalculationSetup setup, T result, DbRefs refs) {

	public static <T> CachedResult<T> of(
		HandlerContext context, CalculationSetup setup, T result) {
		var refs = DbRefs.of(context.db());
		return new CachedResult<T>(setup, result, refs);
	}



}
