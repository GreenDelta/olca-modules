package org.openlca.core.services;

import java.util.UUID;

import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.LcaResult;

record ResultState(
		String id,
		long time,
		CalculationSetup setup,
		LcaResult result,
		String error) {

	public boolean isEmpty() {
		return !isReady() && !isScheduled() && !isError();
	}

	public boolean isReady() {
		return result != null;
	}

	public boolean isScheduled() {
		return setup != null;
	}

	public boolean isError() {
		return error != null;
	}

	static ResultState empty(String id) {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, null, null, null);
	}

	static ResultState schedule(CalculationSetup setup) {
		String id = UUID.randomUUID().toString();
		long time = System.currentTimeMillis();
		return new ResultState(id, time, setup, null, null);
	}

	ResultState toResult(LcaResult result) {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, null, result, null);
	}

	ResultState toError(String error) {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, null, null, error);
	}
}
