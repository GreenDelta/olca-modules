package org.openlca.io.xls.process;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.io.Tests;

public class AllocationTest {

	private final IDatabase db = Tests.getDb();
	private Process synced;

	@Before
	public void setup() {
		var store = InMemoryStore.create();
		var mass = ProcTests.createMass(store);
		var p = Flow.product("p", mass);
		var q = Flow.product("q", mass);
		var e = Flow.elementary("e", mass);

		var process = Process.of("P", p);
		process.defaultAllocationMethod = AllocationMethod.ECONOMIC;
		process.output(q, 1);
		var elemOut = process.output(e, 1);
		store.insert(p, q, e, process); // assigns IDs

		process.allocationFactors.addAll(List.of(
			AllocationFactor.physical(p, 0.2),
			AllocationFactor.physical(q, 0.8),
			AllocationFactor.economic(p, 0.7),
			AllocationFactor.economic(q, 0.3),
			AllocationFactor.causal(p, elemOut, 0.1),
			AllocationFactor.causal(q, elemOut, 0.9)
		));

		synced = ProcTests.syncWithDb(process, store);
	}

	@After
	public void cleanup() {
		db.clear();
	}

	@Test
	public void testCalculate() {
		for (var type : AllocationMethod.values()) {
			if (type == AllocationMethod.NONE)
				continue;
			double expected = switch (type) {
				case PHYSICAL -> 0.2;
				case ECONOMIC, USE_DEFAULT -> 0.7;
				case CAUSAL -> 0.1;
				default -> 1.0;
			};

			var setup = CalculationSetup.of(synced)
				.withAllocation(type);
			var result = new SystemCalculator(db).calculate(setup);
			var enviFlow = result.enviIndex().at(0);
			var flowValue = result.getTotalFlowValueOf(enviFlow);
			assertEquals("failed for type: " + type, expected, flowValue, 1e-17);
		}
	}

	@Test
	public void testCoValues() {
		var q = synced.exchanges.stream()
			.filter(e -> e.flow.name.equals("q"))
			.mapToLong(e -> e.flow.id)
			.findAny()
			.orElseThrow();
		for (var type : AllocationMethod.values()) {
			if (type == AllocationMethod.NONE
				|| type == AllocationMethod.USE_DEFAULT)
				continue;
			var factor = synced.allocationFactors.stream()
				.filter(fi -> fi.method == type && fi.productId == q)
				.findAny()
				.orElseThrow();
			double expected = switch (type) {
				case PHYSICAL -> 0.8;
				case ECONOMIC -> 0.3;
				case CAUSAL -> 0.9;
				default -> 1.0;
			};
			assertEquals(expected, factor.value, 1e-17);
		}
	}

}
