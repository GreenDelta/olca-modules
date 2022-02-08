package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.IResult;

public class CalculationQueue {

	private final IDatabase db;
	private final ConcurrentMap<String, State> states;
	private final ExecutorService threads;

	public CalculationQueue(IDatabase db, int threadCount) {
		this.db = db;
		threads = Executors.newFixedThreadPool(threadCount);
		states = new ConcurrentHashMap<>();
	}

	/**
	 * Calls {@code shutdown} on the underlying thread-pool of this calculation
	 * and returns that thread-pool.
	 *
	 * @return the underlying thread-pool of this calculation so that it is
	 * possible to call additional methods like {@code awaitTermination} on that
	 * thread-pool.
	 */
	public ExecutorService shutdown() {
		threads.shutdown();
		return threads;
	}

	/**
	 * Get the state of the calculation with the given ID.
	 */
	public State get(String id) {
		var state = states.get(id);
		return state == null
			? State.empty(id)
			: state;
	}

	/**
	 * Get the states of all calculations in this queue.
	 */
	public Collection<State> getAll() {
		return new ArrayList<>(states.values());
	}

	/**
	 * Get the states of all calculations in this queue that match the given
	 * predicate.
	 */
	public Collection<State> getAll(Predicate<State> p) {
		return states.values()
			.stream()
			.filter(p)
			.collect(Collectors.toList());
	}

	/**
	 * Removes the calculation or result with the given ID. If the calculation
	 * was scheduled, it will not be performed.
	 */
	public void dispose(String id) {
		states.remove(id);
	}

	/**
	 * Schedules a calculation and returns immediately.
	 */
	public State schedule(CalculationSetup setup) {
		var state = State.schedule(Objects.requireNonNull(setup));
		states.put(state.id, state);
		submit(state.id);
		return state;
	}

	private void submit(String id) {
		threads.submit(() -> {
			var state = states.get(id);
			if (state == null || !state.isScheduled())
				return;
			try {
				var result = new SystemCalculator(db).calculate(state.setup);
				var resultState = state.toResult(result);
				states.put(state.id, resultState);
			} catch (Throwable err) {
				var message = "Calculation failed: " + err.getMessage();
				var errorState = state.toError(message);
				states.put(state.id, errorState);
			}
		});
	}

	public record State(
		String id,
		long time,
		CalculationSetup setup,
		IResult result,
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

		static State empty(String id) {
			long time = System.currentTimeMillis();
			return new State(id, time, null, null, null);
		}

		static State schedule(CalculationSetup setup) {
			String id = UUID.randomUUID().toString();
			long time = System.currentTimeMillis();
			return new State(id, time, setup, null, null);
		}

		State toResult(IResult result) {
			long time = System.currentTimeMillis();
			return new State(id, time, null, result, null);
		}

		State toError(String error) {
			long time = System.currentTimeMillis();
			return new State(id, time, null, null, error);
		}
	}

}
