package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.sd.model.Id;
import org.openlca.sd.xmile.Xmile;

public class NonNegStockTest {

	private Xmile xmile;

	@Before
	public void setup() throws Exception {
		try (var stream = getClass().getResourceAsStream("non-neg-stock.xml")) {
			xmile = Xmile.readFrom(stream).orElseThrow();
		}
	}

	@Test
	public void testSimulation() {

		var sim = Simulator.of(xmile).orElseThrow();
		// Stock starts at 5, inflow=1, outflow=2, net=-1 per step
		// But stock is non-negative, so it should stop at 0
		double[][] expected = {
			{0, 1.00, 5.0},   // initial
			{1, 2.00, 4.0},   // 5 + 1 - 2 = 4
			{2, 3.00, 3.0},   // 4 + 1 - 2 = 3
			{3, 4.00, 2.0},   // 3 + 1 - 2 = 2
			{4, 5.00, 1.0},   // 2 + 1 - 2 = 1
			{5, 6.00, 0.0},   // 1 + 1 - 2 = 0
			{6, 7.00, 0.0},   // 0 + 1 - 1 = 0 (outflow limited)
			{7, 8.00, 0.0},   // 0 + 1 - 1 = 0 (outflow limited)
			{8, 9.00, 0.0},   // 0 + 1 - 1 = 0 (outflow limited)
			{9, 10.00, 0.0}   // 0 + 1 - 1 = 0 (outflow limited)
		};

		var actual = new ArrayList<double[]>();
		sim.forEach(res -> {
			if (res.isError()) {
				fail(res.error());
			} else {
				var state = res.value();
				double stockValue = state.valueOf(Id.of("Stock"))
					.orElseThrow()
					.asNum();
				actual.add(new double[]{
					state.iteration(),
					state.time(),
					stockValue});
			}
		});

		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			double[] ai = actual.get(i);
			double[] ei = expected[i];
			assertArrayEquals(ei, ai, 0.001);
		}
	}

	/// Tests that outflows are constrained when stock would go negative.
	///
	/// Model: Stock starts at 5, inflow=1, outflow1=2, outflow2=2
	/// Net flow per step: 1 - 2 - 2 = -3
	///
	/// The problem: when stock reaches near zero, the outflows must be
	/// constrained so that stock doesn't go negative. The first outflow
	/// gets priority - it takes what it can from available stock + inflow.
	///
	/// Example at step 2:
	///   Stock = 2 (from previous step)
	///   Available = 2 + 1 (inflow) = 3
	///   outflow1 wants 2, gets 2, remaining = 1
	///   outflow2 wants 2, gets 1 (constrained), remaining = 0
	///   Stock = 0
	///
	/// The outflow values should reflect the constrained amounts, not
	/// the original requested amounts.
	///
	/// This is currently not implemented. Also, it is not clear if this should
	/// be implemented this way, or if this is more a display / export thing
	@Test
	@Ignore
	public void testMultipleOutflowsConstrained() throws Exception {
		Xmile multiOutflowXmile;
		try (var stream = getClass().getResourceAsStream("non-neg-stock-multi-outflow.xml")) {
			multiOutflowXmile = Xmile.readFrom(stream).orElseThrow();
		}

		var sim = Simulator.of(multiOutflowXmile).orElseThrow();

		// Stock starts at 5, inflow=1, outflow1=2, outflow2=2
		// Net = 1 - 2 - 2 = -3 per step (when unconstrained)
		//
		// Step 0: Stock = 5 (initial)
		// Step 1: Stock = 5 + 1 - 2 - 2 = 2
		// Step 2: Stock = 2 + 1 - 2 - 1 = 0 (outflow2 constrained to 1)
		// Step 3+: Stock = 0 + 1 - 1 - 0 = 0 (both outflows constrained)
		//
		// Expected outflow values at step 3 (when stock is 0):
		//   available = 0 + 1 = 1
		//   outflow1 wants 2, gets 1
		//   outflow2 wants 2, gets 0
		double[][] expected = {
			// {iteration, time, stock, outflow1, outflow2}
			{0, 1.00, 5.0, 2.0, 2.0},   // initial - outflows are their defined values
			{1, 2.00, 2.0, 2.0, 2.0},   // full outflows, stock = 5 + 1 - 2 - 2 = 2
			{2, 3.00, 0.0, 2.0, 1.0},   // outflow2 constrained: 2 + 1 - 2 - 1 = 0
			{3, 4.00, 0.0, 1.0, 0.0},   // both constrained: 0 + 1 - 1 - 0 = 0
			{4, 5.00, 0.0, 1.0, 0.0},
			{5, 6.00, 0.0, 1.0, 0.0},
			{6, 7.00, 0.0, 1.0, 0.0},
			{7, 8.00, 0.0, 1.0, 0.0},
			{8, 9.00, 0.0, 1.0, 0.0},
			{9, 10.00, 0.0, 1.0, 0.0}
		};

		var actual = new ArrayList<double[]>();
		sim.forEach(res -> {
			if (res.isError()) {
				fail(res.error());
			} else {
				var state = res.value();
				double stockValue = state.valueOf(Id.of("Stock"))
					.orElseThrow()
					.asNum();
				double outflow1Value = state.valueOf(Id.of("outflow1"))
					.orElseThrow()
					.asNum();
				double outflow2Value = state.valueOf(Id.of("outflow2"))
					.orElseThrow()
					.asNum();
				actual.add(new double[]{
					state.iteration(),
					state.time(),
					stockValue,
					outflow1Value,
					outflow2Value});
			}
		});

		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			double[] ai = actual.get(i);
			double[] ei = expected[i];
			assertArrayEquals("Step " + i, ei, ai, 0.001);
		}
	}
}
