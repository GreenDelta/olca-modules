package org.openlca.core.services;

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
import org.openlca.core.services.ResultState.Job.Calculation;
import org.openlca.core.services.ResultState.Job.Simulation;
import org.openlca.core.services.ResultState.Ready;
import org.openlca.core.services.ResultState.Scheduled;
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
		var next = states.computeIfPresent(id, (_, state) -> state.touch());
		return next == null
				? ResultState.empty(id)
				: next;
	}

	/// Removes the calculation or result with the given ID. If the calculation
	/// was scheduled, it will not be performed.
	public void dispose(String id) {
		dispose(states.remove(id));
	}

	/// Disposes the given state after it was removed from the queue. Only a
	/// ready state owns resources here: the job of a scheduled state is owned
	/// by the worker, which disposes it when it detects that the state was
	/// removed.
	private static void dispose(ResultState removed) {
		if (removed instanceof Ready ready) {
			ready.dispose();
		}
	}

	/// Schedules a calculation and returns immediately.
	public ResultState schedule(CalculationSetup setup) {
		var state = ResultState.schedule(setup);
		states.put(state.id(), state);
		submit(state);
		return state;
	}

	/// Schedules a Monte-Carlo-Simulation for the given setup. After
	/// creating the simulator it schedules the first simulation, and
	/// returns the scheduled state, or a failed state if the simulator could
	/// not be created.
	public ResultState scheduleSimulation(CalculationSetup setup) {
		var state = ResultState.scheduleSimulation(setup, db);
		if (state instanceof Scheduled scheduled) {
			states.put(scheduled.id(), scheduled);
			submit(scheduled);
		}
		return state;
	}

	/// Schedules the next run of the simulation with the given ID. The result
	/// of the previous run is disposed; its values are already stored in the
	/// simulation result. If the next run is already scheduled, this method
	/// returns the current state without scheduling another run.
	public ResultState nextSimulation(String id) {
		while (true) {
			var current = states.get(id);
			if (current instanceof Scheduled s && s.job() instanceof Simulation)
				return current;
			if (!(current instanceof Ready ready))
				return ResultState.failed("no simulator available: id=" + id);
			var next = ready.toNextScheduled();
			if (!(next instanceof Scheduled scheduled))
				return next; // not a simulation

			// fails if another thread replaced the state in the meantime
			if (!states.replace(id, ready, scheduled))
				continue;
			// the values of the previous run are already stored in the
			// simulation result, so we can dispose its result
			ready.disposeResult();
			submit(scheduled);
			return scheduled;
		}
	}

	/// Submits the calculation of the given scheduled state to the thread
	/// pool. Only this worker replaces the scheduled state; while it is
	/// scheduled, the worker owns its job and disposes it when the state was
	/// removed in the meantime or when the calculation failed.
	private void submit(Scheduled scheduled) {
		var id = scheduled.id();
		threads.submit(() -> {
			if (!(states.get(id) instanceof Scheduled)) {
				// the calculation was disposed before it started
				scheduled.disposeJob();
				return;
			}
			try {
				var result = switch (scheduled.job()) {
					case Calculation c -> new SystemCalculator(db)
							.withLibraries(libraries)
							.calculate(c.setup());
					case Simulation s -> {
						var r = s.simulator().nextRun();
						if (r == null)
							throw new IllegalStateException("simulation run failed");
						yield r;
					}
				};
				var ready = scheduled.toReady(result);
				var stored = states.computeIfPresent(
						id, (_, current) -> current instanceof Scheduled ? ready : current);
				if (stored != ready) {
					// the calculation was disposed in the meantime
					ready.dispose();
				}
			} catch (Throwable err) {
				var message = "Calculation failed: " + err.getMessage();
				states.computeIfPresent(id, (_, current) -> current instanceof Scheduled
						? scheduled.toFailed(message)
						: current);
				scheduled.disposeJob();
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
					// only remove the state if it was not updated by an access in
					// the meantime; `remove` compares the states with `equals`, so
					// a record copy with the same time-stamp is removed too, which
					// is fine as that copy is expired as well
					if (state.time() < limit
							&& queue.states.remove(e.getKey(), state)) {
						dispose(state);
					}
				}
			}
		}
	}

}
