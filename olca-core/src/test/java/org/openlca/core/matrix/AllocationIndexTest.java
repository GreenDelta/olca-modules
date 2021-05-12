package org.openlca.core.matrix;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.TestProcess;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.expressions.FormulaInterpreter;

public class AllocationIndexTest {

	private final IDatabase db = Tests.getDb();
	private Process process;

	@Before
	public void setup() {
		process = TestProcess
				.refProduct("p1", 1.0, "kg")
				.with(p -> p.defaultAllocationMethod = AllocationMethod.CAUSAL)
				.prodOut("p2", 1.0, "kg")
				.wasteIn("w", 1.0, "kg")
				.elemOut("CO2", 4.0, "kg")

				// economic factors
				.alloc("p1", AllocationMethod.ECONOMIC, 0.1)
				.alloc("p2", AllocationMethod.ECONOMIC, 0.3)
				.alloc("w", AllocationMethod.ECONOMIC, 0.6)

				// physical factors
				.alloc("p1", AllocationMethod.PHYSICAL, 0.4)
				.alloc("p2", AllocationMethod.PHYSICAL, 0.4)
				.alloc("w", AllocationMethod.PHYSICAL, 0.2)

				// causal factors
				.alloc("p1", "CO2", 0.3)
				.alloc("p2", "CO2", 0.2)
				.alloc("w", "CO2", 0.5)
				.get();
	}

	@Test
	public void testDefaultFactor() {
		var idx = index(AllocationMethod.CAUSAL);
		var fi = new FormulaInterpreter();
		// there is no exchange with an ID -999; we should get always 1.0
		assertEquals(1.0, idx.get(product("p1"), -999, fi), 1e-16);
		assertEquals(1.0, idx.get(product("p2"), -999, fi), 1e-16);
		assertEquals(1.0, idx.get(product("w"), -999, fi), 1e-16);
	}

	@Test
	public void testNone() {
		AllocationIndex idx = index(AllocationMethod.NONE);
		// the NONE allocation method should result in default factors
		assertEquals(1.0, factor(idx, "p1"), 1e-16);
		assertEquals(1.0, factor(idx, "p2"), 1e-16);
		assertEquals(1.0, factor(idx, "w"), 1e-16);
	}

	@Test
	public void testNull() {
		AllocationIndex idx = index(null);
		// a null value for the allocation method should result in default
		// factors, too
		assertEquals(1.0, factor(idx, "p1"), 1e-16);
		assertEquals(1.0, factor(idx, "p2"), 1e-16);
		assertEquals(1.0, factor(idx, "w"), 1e-16);
	}

	/**
	 * This test fails if enabled. The caller of `AllocationIndex.get` has to ensure
	 * that the method is only called with exchanges that can be allocated to a
	 * product or waste flow (product inputs, waste outputs, or elementary flows).
	 */
	@Test
	@Ignore
	public void testProductFactors() {
		var idx = index(AllocationMethod.PHYSICAL);
		var fi = new FormulaInterpreter();
		// the allocation factors of the product / waste flows should default to
		// 1

		for (String p : Arrays.asList("p1", "p2", "w")) {
			long exchangeID = TestProcess.findExchange(process, p).id;
			assertEquals(1.0, idx.get(product(p), exchangeID, fi), 1e-16);
		}
	}

	@Test
	public void testDefault() {
		var idx = index(AllocationMethod.USE_DEFAULT);
		assertEquals(0.3, factor(idx, "p1"), 1e-16);
		assertEquals(0.2, factor(idx, "p2"), 1e-16);
		assertEquals(0.5, factor(idx, "w"), 1e-16);
	}

	@Test
	public void testCausal() {
		var idx = index(AllocationMethod.CAUSAL);
		assertEquals(0.3, factor(idx, "p1"), 1e-16);
		assertEquals(0.2, factor(idx, "p2"), 1e-16);
		assertEquals(0.5, factor(idx, "w"), 1e-16);
	}

	@Test
	public void testPhysical() {
		var idx = index(AllocationMethod.PHYSICAL);
		assertEquals(0.4, factor(idx, "p1"), 1e-16);
		assertEquals(0.4, factor(idx, "p2"), 1e-16);
		assertEquals(0.2, factor(idx, "w"), 1e-16);
	}

	@Test
	public void testEconomic() {
		var idx = index(AllocationMethod.ECONOMIC);
		assertEquals(0.1, factor(idx, "p1"), 1e-16);
		assertEquals(0.3, factor(idx, "p2"), 1e-16);
		assertEquals(0.6, factor(idx, "w"), 1e-16);
	}

	private AllocationIndex index(AllocationMethod method) {
		TechIndex techIdx = new TechIndex(product("p1"));
		techIdx.add(product("p2"));
		techIdx.add(product("w"));
		return AllocationIndex.create(db, techIdx, method);
	}

	private double factor(AllocationIndex idx, String product) {
		var fi = new FormulaInterpreter();
		var exchangeID = TestProcess.findExchange(process, "CO2").id;
		return idx.get(product(product), exchangeID, fi);
	}

	private TechFlow product(String name) {
		Exchange e = TestProcess.findExchange(process, name);
		return TechFlow.of(process, e.flow);
	}

}
