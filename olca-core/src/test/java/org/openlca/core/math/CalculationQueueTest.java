package org.openlca.core.math;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.results.SimpleResult;

public class CalculationQueueTest {

	private final IDatabase db = Tests.getDb();
	private Process process;

	@Before
	public void setup() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		var co2 = Flow.elementary("CO2", mass);
		var steel = Flow.product("Steel", mass);
		process = Process.of("Steel production", steel);
		process.output(co2, 2);
		db.insert(units, mass, co2, steel, process);
	}

	@After
	public void tearDown() {
		db.delete(process);
		boolean first = true;
		for (var e : process.exchanges) {
			db.delete(e.flow);
			if (first) {
				first = false;
				continue;
			}
			db.delete(e.flow.referenceFlowProperty);
			db.delete(e.flow.referenceFlowProperty.unitGroup);
		}
	}

	@Test
	public void testCalculateAll() throws Exception {
		// int n = 100_000;
		int n = 100;
		var queue = new CalculationQueue(db, 2);
		var setup = CalculationSetup.simple(process);
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
				var result = (SimpleResult) state.result();
				assertEquals(2, result.getTotalFlowResults().get(0).value, 1e-10);
			}

			ids.removeAll(finished);
			if (!ids.isEmpty()) {
				Thread.sleep(100);
			}
		}
	}

}
