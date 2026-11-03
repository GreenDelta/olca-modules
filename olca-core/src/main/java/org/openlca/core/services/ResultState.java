package org.openlca.core.services;

import java.util.UUID;
import java.util.function.Function;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.LcaResult;

public class ResultState {

	private final String id;
	private final long time;
	private final CalculationSetup setup;
	private final Simulator simulator;
	private final ResultHandle result;
	private final String error;

	private ResultState(
		String id,
		CalculationSetup setup,
		Simulator simulator,
		ResultHandle result,
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

	public String error() {
		return error;
	}

	/// Applies the given function on the result of this state. The result is
	/// not thread-safe and can be disposed by other threads; thus, it must be
	/// only accessed within the given function, which runs under the lock of
	/// the result. Returns an empty response if there is no result.
	public <T> Response<T> withResult(Function<LcaResult, Response<T>> fn) {
		return result != null
			? result.apply(fn)
			: Response.empty();
	}

	/// Like [#withResult(Function)] but with the analysis group results.
	/// Returns an empty response if there are no analysis groups.
	<T> Response<T> withGroupResult(Function<Util.GroupResult, Response<T>> fn) {
		return result != null
			? result.applyWithGroups(fn)
			: Response.empty();
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

	/// Creates a new result state with an updated result. Note that the result
	/// can be also `null`, e.g. if a next simulation result should be
	/// schedules.
	ResultState updateResult(LcaResult result) {
		var handle = result != null
			? new ResultHandle(setup, result)
			: null;
		return new ResultState(id, setup, simulator, handle, null);
	}

	/// Returns a new result state with an updated time-stamp.
	ResultState update() {
		return new ResultState(id, setup, simulator, result, null);
	}

	/// Converts this result state to an error, for example, when the calculation
	/// of the result failed.
	ResultState toError(String error) {
		return new ResultState(id, null, null, null, error);
	}

	/// Disposes the result of this state, if present. This waits until a
	/// running access of the result is finished. Note that all copies of this
	/// state share the same result.
	void dispose() {
		if (result != null) {
			result.dispose();
		}
	}
}
