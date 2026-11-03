package org.openlca.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;

public class CalculationQueueTest {

	private final IDatabase db = Tests.getDb();
	private Process process;
	private List<RootEntity> entities;

	@Before
	public void setup() {
		// todo: this is currently necessary because other
		// tests do not delete their data (but they should)
		db.clear();

		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var co2 = Flow.elementary("CO2", mass);
		var steel = Flow.product("Steel", mass);
		process = Process.of("Steel production", steel);
		process.output(co2, 2);
		entities = List.of(process, steel, co2, mass, units);
		db.insert(units, mass, co2, steel, process);
	}

	@After
	public void tearDown() {
		for (var e : entities) {
			db.delete(e);
		}
	}

	@Test
	public void testCalculateAll() throws Exception {
		// int n = 100_000;
		int n = 100;
		var queue = new CalculationQueue(db, 2);
		var setup = CalculationSetup.of(process);
		var ids = new HashSet<String>();
		for (int i = 0; i < n; i++) {
			var state = queue.schedule(setup);
			assertTrue(state.isScheduled());
			ids.add(state.id());
		}

		// check and wait until we have all results
		while (!ids.isEmpty()) {

			// System.out.println("check " + ids.size() + " states");

			var finished = new HashSet<String>();
			for (var id : ids) {
				var state = queue.get(id);
				if (state.isScheduled())
					continue;
				assertTrue(state.isReady());
				finished.add(id);
				var value = state.withResult(
						r -> Response.of(r.getTotalFlows().get(0).value()));
				assertEquals(2, value.value(), 1e-10);
			}

			ids.removeAll(finished);
			if (!ids.isEmpty()) {
				Thread.sleep(100);
			}
		}
	}

	/// Many threads access the same result while another thread disposes it.
	/// The accesses must never overlap with each other or with the disposal,
	/// and after the disposal no access to the result is possible anymore.
	@Test
	public void testParallelAccessAndDispose() throws Exception {
		var queue = new CalculationQueue(db, 2);
		var id = queue.schedule(CalculationSetup.of(process)).id();
		awaitReady(queue, id);

		int threadCount = 8;
		var active = new AtomicInteger();
		var overlaps = new AtomicInteger();
		var disposed = new AtomicBoolean();
		var accessAfterDispose = new AtomicInteger();
		var values = new AtomicInteger();
		var errors = new ArrayList<Throwable>();
		var start = new CountDownLatch(1);

		var pool = Executors.newFixedThreadPool(threadCount + 1);
		for (int t = 0; t < threadCount; t++) {
			pool.submit(() -> {
				try {
					start.await();
					for (int i = 0; i < 2_000; i++) {
						var r = queue.get(id).withResult(result -> {
							if (active.incrementAndGet() > 1) {
								overlaps.incrementAndGet();
							}
							// these calls use the lazy caches and the solver
							var provider = result.provider();
							provider.solutionOfOne(0);
							provider.totalFlowsOfOne(0);
							double v = result.getTotalFlows().get(0).value();
							if (disposed.get()) {
								accessAfterDispose.incrementAndGet();
							}
							active.decrementAndGet();
							return Response.of(v);
						});
						if (r.isValue()) {
							assertEquals(2, r.value(), 1e-10);
							values.incrementAndGet();
						}
					}
				} catch (Throwable e) {
					synchronized (errors) {
						errors.add(e);
					}
				}
				return null;
			});
		}

		// dispose the result while the other threads access it
		pool.submit(() -> {
			try {
				start.await();
				Thread.sleep(20);
				queue.dispose(id);
				disposed.set(true);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return null;
		});

		start.countDown();
		pool.shutdown();
		assertTrue(pool.awaitTermination(1, TimeUnit.MINUTES));
		queue.shutdown();

		assertTrue(errors.toString(), errors.isEmpty());
		assertEquals(0, overlaps.get());
		assertEquals(0, accessAfterDispose.get());
		assertTrue(values.get() > 0);
		assertTrue(queue.get(id).withResult(r -> Response.of(1)).isEmpty());
	}

	/// After the next simulation run is scheduled, the result of the previous
	/// run must be disposed.
	@Test
	public void testSimulationDisposesPreviousResult() throws Exception {
		var queue = new CalculationQueue(db, 2);
		var state = queue.scheduleSimulation(CalculationSetup.of(process));
		assertFalse(state.error(), state.isError());
		var id = state.id();

		var first = awaitReady(queue, id);
		assertTrue(first.withResult(r -> Response.of(1)).isValue());

		queue.nextSimulation(id);
		var second = awaitReady(queue, id);

		var old = first.withResult(r -> Response.of(1));
		assertTrue(old.isError());
		assertEquals("the result was disposed", old.error());
		var next = second.withResult(
				r -> Response.of(r.getTotalFlows().get(0).value()));
		assertEquals(2, next.value(), 1e-10);

		queue.dispose(id);
		queue.shutdown();
	}

	private ResultState awaitReady(CalculationQueue queue, String id)
			throws InterruptedException {
		for (int i = 0; i < 600; i++) {
			var state = queue.get(id);
			assertFalse(state.error(), state.isError());
			if (state.isReady())
				return state;
			Thread.sleep(50);
		}
		throw new AssertionError("calculation did not finish: " + id);
	}

}
