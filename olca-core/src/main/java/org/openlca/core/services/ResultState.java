package org.openlca.core.services;

import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.LcaResult;

record ResultState(
		String id,
		long time,
		CalculationSetup setup,
		Simulator simulator,
		LcaResult result,
		String error
		) {

	boolean isEmpty() {
		return !isReady() && !isScheduled() && !isError();
	}

	boolean isReady() {
		return result != null;
	}

	boolean isScheduled() {
		return setup != null && result == null;
	}

	boolean isError() {
		return error != null;
	}

	static ResultState empty(String id) {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, null, null, null, null);
	}

	static ResultState error(String message) {
		String id = UUID.randomUUID().toString();
		long time = System.currentTimeMillis();
		return new ResultState(id, time, null, null, null, message);
	}

	static ResultState schedule(CalculationSetup setup) {
		String id = UUID.randomUUID().toString();
		long time = System.currentTimeMillis();
		return new ResultState(id, time, setup, null, null, null);
	}

	static ResultState scheduleSimulation(CalculationSetup setup, IDatabase db) {
		String id = UUID.randomUUID().toString();
		long time = System.currentTimeMillis();
		try {
			var simulator = Simulator.create(setup, db);
			return new ResultState(id, time, setup, simulator, null, null);
		} catch (Exception e) {
			return new ResultState(id, time, null, null, null,
					"failed to create simulator: " + e.getMessage());
		}
	}

	/**
	 * Creates a new result state with an updated result. Note that the result
	 * can be also {@code null}, e.g. if a next simulation result should be
	 * schedules.
	 */
	ResultState updateResult(LcaResult result) {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, setup, simulator, result, null);
	}

	/**
	 * Returns a new result state with an updated time-stamp.
	 */
	ResultState update() {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, setup, simulator, result, null);
	}

	/**
	 * Converts this result state to an error, for example, when the calculation
	 * of the result failed.
	 */
	ResultState toError(String error) {
		long time = System.currentTimeMillis();
		return new ResultState(id, time, null, null, null, error);
	}

	void dispose() {
		if (result != null) {
			result.dispose();
		}
	}
}
