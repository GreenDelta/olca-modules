package org.openlca.core.results;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlca.core.results.Statistics.Histogram;

public class StatisticsTest {

	@Test
	public void testEmptyStatistics() {
		Statistics stats = Statistics.empty();
		// has a 0 value
		assertEquals(0, stats.count);
		assertEquals(0, stats.getPercentileValue(5), 1e-16);
		assertEquals(0, stats.getPercentileValue(95), 1e-16);
		assertEquals(0, stats.max, 1e-16);
		assertEquals(0, stats.mean, 1e-16);
		assertEquals(0, stats.median, 1e-16);
		assertEquals(0, stats.min, 1e-16);
		assertEquals(0, stats.range, 1e-16);
		assertEquals(0, stats.standardDeviation, 1e-16);

		Histogram hist = Statistics.hist(null, 100);
		assertEquals(0, hist.getMaxAbsoluteFrequency());
		assertEquals(0, hist.getAbsoluteFrequency(5));
		assertEquals(0, hist.getAbsoluteFrequency(95));

	}

	@Test
	public void testSimpleStatistics() {
		Statistics stats = Statistics.of(
				new double[] { 1d, 2d, 3d });
		assertEquals(3, stats.count);
		assertEquals(1, stats.getPercentileValue(5), 1e-16);
		assertEquals(2.5, stats.getPercentileValue(95), 1e-16);
		assertEquals(3, stats.max, 1e-16);
		assertEquals(2, stats.mean, 1e-16);
		assertEquals(2, stats.median, 1e-16);
		assertEquals(1, stats.min, 1e-16);
		assertEquals(2, stats.range, 1e-16);
		assertEquals(1, stats.standardDeviation, 1e-16);

		Histogram hist = Statistics.hist(new double[] { 1d, 2d, 3d }, 3);
		assertEquals(1, hist.getMaxAbsoluteFrequency());
		assertEquals(1, hist.getAbsoluteFrequency(0));
		assertEquals(1, hist.getAbsoluteFrequency(1));
		assertEquals(1, hist.getAbsoluteFrequency(2));
		assertEquals(0, hist.getAbsoluteFrequency(3));

	}

	@Test
	public void testZeroValues() {
		Statistics stats = Statistics.of(
				new double[] { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d });
		assertEquals(0d, stats.mean, 1e-16);
		assertEquals(0d, stats.min, 1e-16);
		assertEquals(0d, stats.max, 1e-16);
		assertEquals(0d, stats.standardDeviation, 1e-16);
	}
}
