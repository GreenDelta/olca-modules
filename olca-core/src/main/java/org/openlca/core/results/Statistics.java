package org.openlca.core.results;

import java.util.Arrays;

/**
 * Calculates statistic parameters for a set of numbers. This is mainly used for
 * presenting results of a Monte-Carlo-Simulation.
 */
public class Statistics {

	private final double[] values;

	public final double min;
	public final double max;
	public final int count;
	public final double mean;
	public final double median;
	public final double standardDeviation;
	public final double range;

	public static class Histogram {

		public final Statistics statistics;

		private final int intervalCount;
		private final int[] frequencies;

		private Histogram(Statistics stats, int intervalCount) {
			this.statistics = stats;
			this.intervalCount = intervalCount < 1 ? 1 : intervalCount;
			frequencies = new int[intervalCount];
			for (double val : stats.values) {
				int idx = getInterval(val);
				frequencies[idx] += 1;
			}
		}

		/**
		 * Get the zero-based index of the interval where the given value is
		 * located.
		 */
		public int getInterval(double value) {
			double min = statistics.min;
			double intervalRange = statistics.range / intervalCount;
			int bucket = (int) ((value - min) / intervalRange);
			if (bucket > (intervalCount - 1))
				return intervalCount - 1;
			return bucket;
		}

		public int getAbsoluteFrequency(int interval) {
			if (interval > (intervalCount - 1))
				return 0;
			return frequencies[interval];
		}

		public int getMaxAbsoluteFrequency() {
			int maxFreq = frequencies[0];
			for (int i = 1; i < frequencies.length; i++)
				maxFreq = Math.max(frequencies[i], maxFreq);
			return maxFreq;
		}

	}

	/**
	 * Creates a histogram with associated statistics for the given data and
	 * number of intervals.
	 */
	public static Histogram hist(double[] values, int intervalCount) {
		Statistics stats = new Statistics(values);
		Histogram hist = new Histogram(stats, intervalCount);
		return hist;
	}

	private Statistics(double[] values) {
		if (values == null || values.length == 0) {
			// empty statistics with no values
			this.values = new double[0];
			this.min = 0;
			this.max = 0;
			this.count = 0;
			this.mean = 0;
			this.median = 0;
			this.standardDeviation = 0;
			this.range = 0;

		} else {
			this.values = new double[values.length];
			System.arraycopy(values, 0, this.values, 0, values.length);
			Arrays.sort(this.values);
			double[] vals = this.values;
			this.count = vals.length;
			this.min = vals[0];
			this.max = vals[vals.length - 1];
			this.range = this.max - this.min;

			// mean
			double sum = 0;
			for (double v : vals) {
				sum += v;
			}
			this.mean = sum / this.count;

			// standard deviation
			if (vals.length < 2) {
				this.standardDeviation = 0;
			} else {
				double sd = 0d;
				for (double v : vals) {
					sd += Math.pow(v - this.mean, 2);
				}
				sd /= vals.length - 1;
				this.standardDeviation = Math.sqrt(sd);
			}

			// median
			if (1 == (this.count % 2)) {
				this.median = vals[this.count / 2];
			} else {
				int upper = this.count / 2;
				int lower = upper - 1;
				this.median = (vals[upper] + vals[lower]) / 2d;
			}
		}
	}

	public static Statistics of(double[] vals) {
		return new Statistics(vals);
	}

	public static Statistics empty() {
		return new Statistics(null);
	}

	/**
	 * Returns the value at the given percentile.
	 *
	 * @param percentile
	 *            the percentage value (0..100)
	 */
	public double getPercentileValue(int percentile) {
		if (values.length == 0)
			return 0;
		int index = percentile * values.length / 100;
		if (index == 0 || 1 == (index % 2))
			return values[index];
		return (values[index] + values[index - 1]) / 2;
	}
}
