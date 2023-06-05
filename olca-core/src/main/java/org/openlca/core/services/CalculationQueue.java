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
		if (db.hasLibraries()) {
			var libDir = config.dataDir().getLibraryDir();
		  queue.withLibraries(LibReaderRegistry.of(libDir, db));
		}
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

	/**
	 * Calls {@code shutdown} on the underlying thread-pool of this calculation
	 * and returns that thread-pool.
	 *
	 * @return the underlying thread-pool of this calculation so that it is
	 * possible to call additional methods like {@code awaitTermination} on that
	 * thread-pool.
	 */
	public ExecutorService shutdown() {
		if (cleaner != null) {
			cleaner.halt();
		}
		threads.shutdown();
		return threads;
	}

	/**
	 * Get the state of the calculation with the given ID.
	 */
	public ResultState get(String id) {
		var next = states.compute(id, ($, state) -> state != null
				? state.update()
				: null);
		return next == null
				? ResultState.empty(id)
				: next;
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
	public ResultState schedule(CalculationSetup setup) {
		var state = ResultState.schedule(Objects.requireNonNull(setup));
		states.put(state.id(), state);
		submit(state.id());
		return state;
	}

	/**
	 * Schedules a Monte-Carlo-Simulation for the given setup. After
	 * creating the simulator it schedules the first simulation, and
	 * returns the state as 'scheduled'.
	 */
	public ResultState scheduleSimulation(CalculationSetup setup) {
		var state = ResultState.scheduleSimulation(setup, db);
		if (state.isError())
			return state;
		states.put(state.id(), state);
		submit(state.id());
		return state;
	}

	public ResultState nextSimulation(String id) {
		var state = states.get(id);
		if (state == null || state.isEmpty() || state.simulator() == null)
			return ResultState.error("no simulator available: id=" + id);
		if (state.isError())
			return state;
		if (state.isScheduled())
			return state;
		var next = state.updateResult(null);
		states.put(id, next);
		submit(id);
		return next;
	}

	private void submit(String id) {
		threads.submit(() -> {
			var state = states.get(id);
			if (state == null || !state.isScheduled())
				return;
			try {
				LcaResult result;
				if (state.simulator() != null) {
					result = state.simulator().nextRun();
				} else {
					result = new SystemCalculator(db)
							.withLibraries(libraries)
							.calculate(state.setup());
				}
				var nextState = state.updateResult(result);
				states.put(state.id(), nextState);
			} catch (Throwable err) {
				var message = "Calculation failed: " + err.getMessage();
				states.put(state.id(), state.toError(message));
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
					if (state.time() < limit) {
						queue.states.remove(e.getKey(), e.getValue());
					}
				}
			}
		}
	}

}
