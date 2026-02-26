package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.xmile.Xmile;

public class ArrayProjectionTest {

	private Xmile xmile;

	@Before
	public void setup() throws Exception {
		try (var stream = getClass().getResourceAsStream("array-projection.xml")) {
			xmile = Xmile.readFrom(stream).orElseThrow();
		}
	}

	@Test
	public void testSimulation() {
		// This test replicates an array projection issue:
		//
		// Stock has dimension [Product] with shape [2]
		// Outflow has dimensions [Product, Region] with shape [2, 2]
		//
		// When subtracting the outflow from the stock, the simulator needs to
		// "project" (sum) the outflow along the Region dimension to match the
		// stock's shape.
		//
		// Expected behavior:
		//   outflow_projected[A] = outflow[A,North] + outflow[A,South] = 1 + 2 = 3
		//   outflow_projected[B] = outflow[B,North] + outflow[B,South] = 3 + 4 = 7
		//
		// Stock dynamics (if projection works):
		//   Stock[A]: 10 + 1 - 3 = 8 (first step)
		//   Stock[B]: 20 + 2 - 7 = 15 (first step)

		var sim = Simulator.of(xmile).orElseThrow();

		double[][] expectedA = {
			{0, 1.00, 10.0},  // initial
			{1, 2.00, 8.0},   // 10 + 1 - 3 = 8
			{2, 3.00, 6.0},   // 8 + 1 - 3 = 6
			{3, 4.00, 4.0},   // 6 + 1 - 3 = 4
			{4, 5.00, 2.0},   // 4 + 1 - 3 = 2
			{5, 6.00, 0.0},   // 2 + 1 - 3 = 0
			{6, 7.00, 0.0},   // non-negative constraint
			{7, 8.00, 0.0},
			{8, 9.00, 0.0},
			{9, 10.00, 0.0}
		};

		double[][] expectedB = {
			{0, 1.00, 20.0},  // initial
			{1, 2.00, 15.0},  // 20 + 2 - 7 = 15
			{2, 3.00, 10.0},  // 15 + 2 - 7 = 10
			{3, 4.00, 5.0},   // 10 + 2 - 7 = 5
			{4, 5.00, 0.0},   // 5 + 2 - 7 = 0
			{5, 6.00, 0.0},   // non-negative constraint
			{6, 7.00, 0.0},
			{7, 8.00, 0.0},
			{8, 9.00, 0.0},
			{9, 10.00, 0.0}
		};

		var actualA = new ArrayList<double[]>();
		var actualB = new ArrayList<double[]>();
		sim.forEach(res -> {
			if (res.isError()) {
				fail(res.error());
			} else {
				var state = res.value();
				var stockTensor = state.valueOf(Id.of("Stock"))
					.orElseThrow()
					.asTensorCell()
					.value();
				double aValue = stockTensor.get(Subscript.of("A")).asNum();
				double bValue = stockTensor.get(Subscript.of("B")).asNum();
				actualA.add(new double[]{
					state.iteration(),
					state.time(),
					aValue});
				actualB.add(new double[]{
					state.iteration(),
					state.time(),
					bValue});
			}
		});

		assertEquals(expectedA.length, actualA.size());
		for (int i = 0; i < expectedA.length; i++) {
			double[] ai = actualA.get(i);
			double[] ei = expectedA[i];
			assertArrayEquals("Product A at step " + i, ei, ai, 0.001);
		}

		assertEquals(expectedB.length, actualB.size());
		for (int i = 0; i < expectedB.length; i++) {
			double[] ai = actualB.get(i);
			double[] ei = expectedB[i];
			assertArrayEquals("Product B at step " + i, ei, ai, 0.001);
		}
	}
}
