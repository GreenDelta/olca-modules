package org.openlca.core.services;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.Simulator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.LcaResult;

/// The state of a calculation in the [CalculationQueue]. States are immutable;
/// a state transition returns a new state.
public sealed interface ResultState {

	String id();

	/// The time of the last access of the state in milliseconds.
	long time();

	/// Returns a copy of this state with the current time as time-stamp.
	ResultState touch();

	static Empty empty(String id) {
		return new Empty(id, now());
	}

	static Failed failed(String error) {
		return new Failed(newId(), now(), error);
	}

	static Scheduled schedule(CalculationSetup setup) {
		var job = new Job.Calculation(Objects.requireNonNull(setup));
		return new Scheduled(newId(), now(), job);
	}

	/// Creates the simulator for the given setup and returns the scheduled
	/// state of the first simulation run, or a failed state if the simulator
	/// could not be created.
	static ResultState scheduleSimulation(CalculationSetup setup, IDatabase db) {
		try {
			var simulator = Simulator.create(setup, db);
			var job = new Job.Simulation(setup, simulator);
			return new Scheduled(newId(), now(), job);
		} catch (Exception e) {
			return failed("failed to create simulator: " + e.getMessage());
		}
	}

	private static String newId() {
		return UUID.randomUUID().toString();
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	private static void dispose(Job job) {
		if (job instanceof Job.Simulation(_, var simulator)) {
			simulator.dispose();
		}
	}

	/// There is no calculation with the given ID.
	record Empty(String id, long time) implements ResultState {

		@Override
		public Empty touch() {
			return new Empty(id, now());
		}
	}

	/// The calculation failed with the given error.
	record Failed(String id, long time, String error) implements ResultState {

		@Override
		public Failed touch() {
			return new Failed(id, now(), error);
		}
	}

	/// The calculation of the job is scheduled or running. While the state
	/// is scheduled, the job is owned by the worker of the calculation queue
	/// that runs it.
	record Scheduled(String id, long time, Job job) implements ResultState {

		@Override
		public Scheduled touch() {
			return new Scheduled(id, now(), job);
		}

		/// Returns the ready state with the given result; the ready state
		/// takes over the job.
		Ready toReady(LcaResult result) {
			var handle = new ResultHandle(job.setup(), Objects.requireNonNull(result));
			return new Ready(id, now(), job, handle);
		}

		/// Returns the failed state with the given error; the failed state
		/// does not take over the job, see [#disposeJob()].
		Failed toFailed(String error) {
			return new Failed(id, now(), error);
		}

		/// Releases the resources of the job. This is called by the worker
		/// that owns the job, when the state was removed from the queue or
		/// when the calculation failed.
		void disposeJob() {
			ResultState.dispose(job);
		}
	}

	/// The calculation is finished and the result is available. The ready
	/// state owns its job and result, see [#dispose()]. The result can be only
	/// accessed via [#withResult(Function)].
	record Ready(
		String id, long time, Job job, ResultHandle result
	) implements ResultState {

		@Override
		public Ready touch() {
			return new Ready(id, now(), job, result);
		}

		/// Applies the given function on the result of this state. The result
		/// is not thread-safe and can be disposed by other threads; thus, it
		/// must be only accessed within the given function, which runs under
		/// the lock of the result. All copies of this state share the same
		/// result.
		public <T> Response<T> withResult(Function<LcaResult, Response<T>> fn) {
			return result.apply(fn);
		}

		/// Like [#withResult(Function)] but with the analysis group results.
		/// Returns an empty response if there are no analysis groups.
		<T> Response<T> withGroupResult(Function<Util.GroupResult, Response<T>> fn) {
			return result.applyWithGroups(fn);
		}

		/// Returns the scheduled state of the next simulation run, or a failed
		/// state if the job of this state is not a simulation. The scheduled
		/// state takes over the simulation; this state then only owns its
		/// result, see [#disposeResult()].
		ResultState toNextScheduled() {
			return job instanceof Job.Simulation
				? new Scheduled(id, now(), job)
				: new Failed(id, now(), "no simulator available: id=" + id);
		}

		/// Disposes the result and the job of this state. This waits until a
		/// running access of the result is finished.
		void dispose() {
			result.dispose();
			ResultState.dispose(job);
		}

		/// Disposes only the result of this state, but not the job.
		void disposeResult() {
			result.dispose();
		}
	}

	/// The calculation of a result state: a single calculation or a run of a
	/// Monte-Carlo simulation.
	sealed interface Job {

		CalculationSetup setup();

		record Calculation(CalculationSetup setup) implements Job {
		}

		record Simulation(
			CalculationSetup setup, Simulator simulator
		) implements Job {
		}
	}
}
