package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.xmile.Xmile;

public class NonNegStockArrayedTest {

	private Xmile xmile;

	@Before
	public void setup() throws Exception {
		try (var stream = getClass().getResourceAsStream("non-neg-stock-arrayed.xml")) {
			xmile = Xmile.readFrom(stream).orElseThrow();
		}
	}

	@Test
	public void testSimulation() {

		var sim = Simulator.of(xmile).orElseThrow();
		// North: Stock starts at 5, inflow=1, outflow=2, net=-1 per step
		// South: Stock starts at 10, inflow=2, outflow=3, net=-1 per step
		// Both are non-negative, so they should stop at 0
		double[][] expectedNorth = {
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

		double[][] expectedSouth = {
			{0, 1.00, 10.0},  // initial
			{1, 2.00, 9.0},   // 10 + 2 - 3 = 9
			{2, 3.00, 8.0},   // 9 + 2 - 3 = 8
			{3, 4.00, 7.0},   // 8 + 2 - 3 = 7
			{4, 5.00, 6.0},   // 7 + 2 - 3 = 6
			{5, 6.00, 5.0},   // 6 + 2 - 3 = 5
			{6, 7.00, 4.0},   // 5 + 2 - 3 = 4
			{7, 8.00, 3.0},   // 4 + 2 - 3 = 3
			{8, 9.00, 2.0},   // 3 + 2 - 3 = 2
			{9, 10.00, 1.0}   // 2 + 2 - 3 = 1
		};

		var actualNorth = new ArrayList<double[]>();
		var actualSouth = new ArrayList<double[]>();
		sim.forEach(res -> {
			if (res.isError()) {
				fail(res.error());
			} else {
				var state = res.value();
				var stockTensor = state.valueOf(Id.of("Stock"))
					.orElseThrow()
					.asTensorCell()
					.value();
				double northValue = stockTensor.get(Subscript.of("North")).asNum();
				double southValue = stockTensor.get(Subscript.of("South")).asNum();
				actualNorth.add(new double[]{
					state.iteration(),
					state.time(),
					northValue});
				actualSouth.add(new double[]{
					state.iteration(),
					state.time(),
					southValue});
			}
		});

		assertEquals(expectedNorth.length, actualNorth.size());
		for (int i = 0; i < expectedNorth.length; i++) {
			double[] ai = actualNorth.get(i);
			double[] ei = expectedNorth[i];
			assertArrayEquals("North at step " + i, ei, ai, 0.001);
		}

		assertEquals(expectedSouth.length, actualSouth.size());
		for (int i = 0; i < expectedSouth.length; i++) {
			double[] ai = actualSouth.get(i);
			double[] ei = expectedSouth[i];
			assertArrayEquals("South at step " + i, ei, ai, 0.001);
		}
	}
}
