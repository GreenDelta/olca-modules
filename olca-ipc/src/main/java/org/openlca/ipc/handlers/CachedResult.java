package org.openlca.ipc.handlers;

import org.openlca.core.math.CalculationSetup;
import org.openlca.core.results.SimpleResult;

class CachedResult {

	CalculationSetup setup;
	SimpleResult result;

	public static CachedResult of(CalculationSetup setup, SimpleResult result) {
		CachedResult r = new CachedResult();
		r.setup = setup;
		r.result = result;
		return r;
	}

}
