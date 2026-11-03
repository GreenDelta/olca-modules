package org.openlca.core.services;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReaderRegistry;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.LcaResult;
import org.slf4j.LoggerFactory;

public class CalculationQueue {

	private final IDatabase db;
	private final ConcurrentMap<String, ResultState> states;
	private final ExecutorService threads;
	private LibReaderRegistry libraries;
	private Cleaner cleaner;

	public CalculationQueue(IDatabase db, int threadCount) {
		this.db = db;
		threads = Executors.newFixedThreadPool(threadCount);
		states = new ConcurrentHashMap<>();
	}

	public CalculationQueue withLibraries(LibReaderRegistry libraries) {
		this.libraries = libraries;
		return this;
	}

	public static CalculationQueue of(ServerConfig config) {
		var db = config.db();
		var queue = new CalculationQueue(db, config.threadCount());
		config.libraries().ifPresent(queue::withLibraries);
		if (config.timeout() > 0) {
			queue.withTimeout(config.timeout(), TimeUnit.MINUTES);
		}
		return queue;
	}

	public void withTimeout(long time, TimeUnit unit) {
		if (cleaner != null) {
			cleaner.halt();
		}
		if (time <= 0)
			return;
		long timeout = unit.toMillis(time);
		cleaner = new Cleaner(this, timeout);
		cleaner.start();
	}

	/// Calls `shutdown` on the underlying thread-pool of this calculation
	/// and returns that thread-pool.
	///
	/// @return the underlying thread-pool of this calculation so that it is
	/// possible to call additional methods like `awaitTermination` on that
	/// thread-pool.
	public ExecutorService shutdown() {
		if (cleaner != null) {
			cleaner.halt();
		}
		threads.shutdown();
		return threads;
	}

	/// Get the state of the calculation with the given ID.
	public ResultState get(String id) {
		var next = states.compute(id, (_, state) -> state != null
				? state.update()
				: null);
		return next == null
				? ResultState.empty(id)
				: next;
	}

	/// Removes the calculation or result with the given ID. If the calculation
	/// was scheduled, it will not be performed.
	public void dispose(String id) {
		var state = states.remove(id);
		if (state != null) {
			state.dispose();
		}
	}

	/// Schedules a calculation and returns immediately.
	public ResultState schedule(CalculationSetup setup) {
		var state = ResultState.schedule(Objects.requireNonNull(setup));
		states.put(state.id(), state);
		submit(state);
		return state;
	}

	/// Schedules a Monte-Carlo-Simulation for the given setup. After
	/// creating the simulator it schedules the first simulation, and
	/// returns the state as 'scheduled'.
	public ResultState scheduleSimulation(CalculationSetup setup) {
		var state = ResultState.scheduleSimulation(setup, db);
		if (state.isError())
			return state;
		states.put(state.id(), state);
		submit(state);
		return state;
	}

	/// Schedules the next run of the simulation with the given ID. The result
	/// of the previous run is disposed; its values are already stored in the
	/// simulation result. If the next run is already scheduled, this method
	/// returns the current state without scheduling another run.
	public ResultState nextSimulation(String id) {
		while (true) {
			var current = states.get(id);
			if (current == null || current.simulator() == null)
				return ResultState.error("no simulator available: id=" + id);
			if (current.isError() || current.isScheduled())
				return current;
			var next = current.toNextRun();
			// fails if another thread replaced the state in the meantime
			if (!states.replace(id, current, next))
				continue;
			// the values of the previous run are already stored in the
			// simulation result, so we can dispose its result
			current.disposeResult();
			submit(next);
			return next;
		}
	}

	/// Submits the calculation of the given scheduled state to the thread
	/// pool. While the state is scheduled, the worker owns a possible
	/// simulator of that state: it disposes the simulator when the state was
	/// removed in the meantime or when the calculation failed.
	private void submit(ResultState scheduled) {
		var id = scheduled.id();
		threads.submit(() -> {
			var state = states.get(id);
			if (state == null) {
				// the calculation was disposed before it started
				scheduled.disposeSimulator();
				return;
			}
			if (!state.isScheduled())
				return;
			try {

				LcaResult result;
				if (state.simulator() != null) {
					result = state.simulator().nextRun();
					if (result == null)
						throw new IllegalStateException("simulation run failed");
				} else {
					result = new SystemCalculator(db)
							.withLibraries(libraries)
							.calculate(state.setup());
				}

				var nextState = state.toReady(result);
				var stored = states.computeIfPresent(
						id, (_, current) -> current.isScheduled() ? nextState : current);
				if (stored != nextState) {
					// the calculation was disposed in the meantime
					nextState.dispose();
				}
			} catch (Throwable err) {
				var message = "Calculation failed: " + err.getMessage();
				states.computeIfPresent(id, (_, current) -> current.isScheduled()
						? state.toError(message)
						: current);
				// the error state does not take over the simulator
				state.disposeSimulator();
			}
		});
	}

	private static class Cleaner extends Thread {

		private final CalculationQueue queue;
		private final long timeout;
		private final AtomicBoolean stopped;

		Cleaner(CalculationQueue queue, long timeout) {
			this.queue = queue;
			this.timeout = timeout;
			this.stopped = new AtomicBoolean(false);
			setDaemon(true);

		}

		void halt() {
			this.stopped.set(true);
		}

		@Override
		public void run() {
			while (!stopped.get()) {
				try {
					Thread.sleep(timeout / 2);
				} catch (InterruptedException e) {
					var log = LoggerFactory.getLogger(getClass());
					log.error("failed to wait in calculation queue");
					stopped.set(true);
				}

				long limit = System.currentTimeMillis() - timeout;
				for (var e : queue.states.entrySet()) {
					var state = e.getValue();
					// only dispose the result if the state was not updated by an
					// access in the meantime
					if (state.time() < limit
							&& queue.states.remove(e.getKey(), state)) {
						state.dispose();
					}
				}
			}
		}
	}

}
