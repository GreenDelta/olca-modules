package org.openlca.core.services;

import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.LcaResult;
import org.openlca.core.results.agroups.AnalysisGroupResult;

public class ResultState {

	private final String id;
	private final long time;
	private final CalculationSetup setup;
	private final Simulator simulator;
	private final LcaResult result;
	private final String error;

	private AnalysisGroupResult groupResult;

	private ResultState(
			String id,
			CalculationSetup setup,
			Simulator simulator,
			LcaResult result,
			String error) {
		this.id = id;
		this.time = System.currentTimeMillis();
		this.setup = setup;
		this.simulator = simulator;
		this.result = result;
		this.error = error;
	}

	public String id() {
		return id;
	}

	public long time() {
		return time;
	}

	public CalculationSetup setup() {
		return setup;
	}

	public Simulator simulator() {
		return simulator;
	}

	public LcaResult result() {
		return result;
	}

	public String error() {
		return error;
	}

	AnalysisGroupResult groupResult() {
		if (groupResult != null)
			return groupResult;
		if (setup == null || result == null)
			return AnalysisGroupResult.empty();

		synchronized (this) {
			if (groupResult != null)
				return groupResult;
			if (!(setup.target() instanceof ProductSystem sys)
					|| sys.analysisGroups.isEmpty()) {
				groupResult = AnalysisGroupResult.empty();
			} else {
				groupResult = AnalysisGroupResult.of(sys, result);
			}
			return groupResult;
		}
	}

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
		return new ResultState(id, null, null, null, null);
	}

	static ResultState error(String message) {
		String id = UUID.randomUUID().toString();
		return new ResultState(id, null, null, null, message);
	}

	static ResultState schedule(CalculationSetup setup) {
		String id = UUID.randomUUID().toString();
		return new ResultState(id, setup, null, null, null);
	}

	static ResultState scheduleSimulation(CalculationSetup setup, IDatabase db) {
		String id = UUID.randomUUID().toString();
		try {
			var simulator = Simulator.create(setup, db);
			return new ResultState(id, setup, simulator, null, null);
		} catch (Exception e) {
			return new ResultState(id, null, null, null,
					"failed to create simulator: " + e.getMessage());
		}
	}

	/**
	 * Creates a new result state with an updated result. Note that the result
	 * can be also {@code null}, e.g. if a next simulation result should be
	 * schedules.
	 */
	ResultState updateResult(LcaResult result) {
		return new ResultState(id, setup, simulator, result, null);
	}

	/**
	 * Returns a new result state with an updated time-stamp.
	 */
	ResultState update() {
		return new ResultState(id, setup, simulator, result, null);
	}

	/**
	 * Converts this result state to an error, for example, when the calculation
	 * of the result failed.
	 */
	ResultState toError(String error) {
		return new ResultState(id, null, null, null, error);
	}

	void dispose() {
		if (result != null) {
			result.dispose();
		}
	}
}
