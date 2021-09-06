package org.openlca.ipc.handlers;

import org.openlca.core.model.CalculationSetup;

class CachedResult<T> {

	CalculationSetup setup;
	T result;

	public static <T> CachedResult<T> of(CalculationSetup setup, T result) {
		CachedResult<T> r = new CachedResult<T>();
		r.setup = setup;
		r.result = result;
		return r;
	}
}
