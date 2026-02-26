package org.openlca.sd.eqn;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.openlca.sd.model.Id;
import org.openlca.sd.xmile.Xmile;

public class SheepTest {

	private Xmile xmile;

	@Before
	public void setup() throws Exception {
		try (var stream = getClass().getResourceAsStream("sheep.xml")) {
			xmile = Xmile.readFrom(stream).orElseThrow();
		}
	}

	@Test
	public void testSimulation() {

		var sim = Simulator.of(xmile).orElseThrow();
		double[][] expected = {
			{0, 1.00, 10.000},
			{1, 2.00, 12.000},
			{2, 3.00, 14.400},
			{3, 4.00, 17.280},
			{4, 5.00, 20.736},
			{5, 6.00, 24.883},
			{6, 7.00, 29.859},
			{7, 8.00, 35.831},
			{8, 9.00, 42.998},
			{9, 10.00, 51.597}
		};

		var actual = new ArrayList<double[]>();
		sim.forEach(res -> {
			if (res.isError()) {
				fail(res.error());
			} else {
				var state = res.value();
				double sheepValue = state.valueOf(Id.of("sheep"))
					.orElseThrow()
					.asNum();
				actual.add(new double[]{
					state.iteration(),
					state.time(),
					sheepValue});
			}
		});

		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			double[] ai = actual.get(i);
			double[] ei = expected[i];
			assertArrayEquals(ai, ei, 0.001);
		}
	}
}
