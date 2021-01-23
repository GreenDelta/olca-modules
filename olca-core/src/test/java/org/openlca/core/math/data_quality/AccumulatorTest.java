package org.openlca.core.math.data_quality;

import static org.junit.Assert.*;

import org.junit.Test;

public class AccumulatorTest {

	private final byte[] dqs = {1, 0, 4};
	private final double[] weights = {0.5, 0.1, 0.4};

	@Test
	public void testNoCeilingExcludeNA() {
		var setup = new DQCalculationSetup();
		setup.ceiling = false;
		setup.naHandling = NAHandling.EXCLUDE;
		setup.aggregationType = AggregationType.WEIGHTED_AVERAGE;
		check(setup, 2);
	}

	@Test
	public void testCeilingExcludeNA() {
		var setup = new DQCalculationSetup();
		setup.ceiling = true;
		setup.naHandling = NAHandling.EXCLUDE;
		setup.aggregationType = AggregationType.WEIGHTED_AVERAGE;
		check(setup, 3);
	}

	@Test
	public void testNoCeilingNaToMax() {
		var setup = new DQCalculationSetup();
		setup.ceiling = false;
		setup.naHandling = NAHandling.USE_MAX;
		setup.aggregationType = AggregationType.WEIGHTED_AVERAGE;
		check(setup, 3);
	}

	@Test
	public void testAggTypeMax() {
		var setup = new DQCalculationSetup();
		setup.ceiling = false;
		setup.naHandling = NAHandling.EXCLUDE;
		setup.aggregationType = AggregationType.MAXIMUM;
		check(setup, 4);

		// now with na-handling -> max
		setup.naHandling = NAHandling.USE_MAX;
		check(setup, 5);
	}

	@Test
	public void testAggTypeSquaredWeights() {
		var setup = new DQCalculationSetup();
		setup.ceiling = false;
		setup.naHandling = NAHandling.EXCLUDE;
		setup.aggregationType = AggregationType.WEIGHTED_SQUARED_AVERAGE;
		check(setup, 2);

		// now with na-handling -> max
		setup.naHandling = NAHandling.USE_MAX;
		check(setup, 2);
	}

	private void check(DQCalculationSetup setup, int result) {
		var acc = new Accumulator(setup, (byte) 5);
		assertEquals(result, acc.get(dqs, weights));

		// add some random stuff and reset
		for (int i = 0; i < 100; i++) {
			acc.add((byte) (i / 10), Math.random() * 100);
		}
		acc.reset();

		// test multi adding
		acc.addAll(dqs, weights);
		assertEquals(result, acc.get());
		acc.reset();

		// test single adding
		for (int i = 0; i < dqs.length; i++) {
			acc.add(dqs[i], weights[i]);
		}
		assertEquals(result, acc.get());
	}
}
