package org.openlca.core.results;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class StatisticsTest {

	@Test
	public void testEmptyStatistics() {
		SimulationStatistics statistics = SimulationStatistics.empty();
		// has a 0 value
		assertEquals(1, statistics.getMaximalAbsoluteFrequency());
		assertEquals(1, statistics.getCount());
		assertEquals(0, statistics.getPercentileValue(5), 1e-16);
		assertEquals(0, statistics.getPercentileValue(95), 1e-16);
		assertEquals(0, statistics.getAbsoluteFrequency(5));
		assertEquals(0, statistics.getAbsoluteFrequency(95));
		assertEquals(0, statistics.getMaximum(), 1e-16);
		assertEquals(0, statistics.getMean(), 1e-16);
		assertEquals(0, statistics.getMedian(), 1e-16);
		assertEquals(0, statistics.getMinimum(), 1e-16);
		assertEquals(0, statistics.getRange(), 1e-16);
		assertEquals(0, statistics.getStandardDeviation(), 1e-16);
	}

	@Test
	public void testSimpleStatistics() {
		SimulationStatistics statistics = new SimulationStatistics(
				Arrays.asList(1d, 2d, 3d), 3);
		// has a 0 value
		assertEquals(1, statistics.getMaximalAbsoluteFrequency());
		assertEquals(3, statistics.getCount());
		assertEquals(1, statistics.getPercentileValue(5), 1e-16);
		assertEquals(2.5, statistics.getPercentileValue(95), 1e-16);
		assertEquals(1, statistics.getAbsoluteFrequency(0));
		assertEquals(1, statistics.getAbsoluteFrequency(1));
		assertEquals(1, statistics.getAbsoluteFrequency(2));
		assertEquals(0, statistics.getAbsoluteFrequency(3));
		assertEquals(3, statistics.getMaximum(), 1e-16);
		assertEquals(2, statistics.getMean(), 1e-16);
		assertEquals(2, statistics.getMedian(), 1e-16);
		assertEquals(1, statistics.getMinimum(), 1e-16);
		assertEquals(2, statistics.getRange(), 1e-16);
		assertEquals(1, statistics.getStandardDeviation(), 1e-16);
	}

	@Test
	public void testZeroValues() {
		SimulationStatistics statistics = new SimulationStatistics(
				Arrays.asList(0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d), 10);
		assertEquals(0d, statistics.getMean(), 1e-16);
		assertEquals(0d, statistics.getMinimum(), 1e-16);
		assertEquals(0d, statistics.getMaximum(), 1e-16);
		assertEquals(0d, statistics.getStandardDeviation(), 1e-16);
	}
}
