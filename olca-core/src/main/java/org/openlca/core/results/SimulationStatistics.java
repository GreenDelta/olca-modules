package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculates statistic parameters for a set of numbers.
 */
public class SimulationStatistics {

	private List<Double> values;
	private int intervalCount;
	private int[] frequencies;

	public SimulationStatistics(List<Double> values, int intervalCount) {
		if (values == null || values.isEmpty()) {
			this.values = new ArrayList<>();
			this.values.add(0d);
		} else {
			this.values = new ArrayList<>(values);
		}
		Collections.sort(this.values);
		this.intervalCount = intervalCount < 1 ? 1 : intervalCount;
		calculateFrequencyTable();
	}

	public static SimulationStatistics empty() {
		return new SimulationStatistics(null, 1);
	}

	private void calculateFrequencyTable() {
		frequencies = new int[intervalCount];
		for (double val : values) {
			int idx = getInterval(val);
			frequencies[idx] += 1;
		}
	}

	/**
	 * Get the zero-based index of the interval where the given value is
	 * located.
	 */
	public int getInterval(double value) {
		double min = getMinimum();
		double intervalRange = getRange() / intervalCount;
		int bucket = (int) ((value - min) / intervalRange);
		if (bucket > (intervalCount - 1))
			return intervalCount - 1;
		return bucket;
	}

	public double getMaximum() {
		return values.get(values.size() - 1);
	}

	public double getMinimum() {
		return values.get(0);
	}

	public int getCount() {
		return values.size();
	}

	public double getMean() {
		double sum = 0;
		for (double v : values) {
			sum += v;
		}
		return sum / values.size();
	}

	/**
	 * Returns the value at the given percentile.
	 * 
	 * @param percentile
	 *            the percentage value (0..100)
	 */
	public double getPercentileValue(int percentile) {
		int index = percentile * values.size() / 100;
		if (index == 0 || 1 == (index % 2))
			return values.get(index);
		return (values.get(index) + values.get(index - 1)) / 2;
	}

	public double getMedian() {
		int size = getCount();
		if (size == 0)
			return 0;
		if (1 == (size % 2))
			return values.get(size / 2);
		int upper = size / 2;
		int lower = upper - 1;
		return (values.get(upper) + values.get(lower)) / 2d;
	}

	public double getStandardDeviation() {
		if (values.size() < 2)
			return 0d;
		double mean = getMean();
		double sd = 0d;
		for (double val : values)
			sd += Math.pow(val - mean, 2);
		sd /= values.size() - 1;
		return Math.sqrt(sd);
	}

	public double getRange() {
		return getMaximum() - getMinimum();
	}

	public int getAbsoluteFrequency(int interval) {
		if (interval > (intervalCount - 1))
			return 0;
		return frequencies[interval];
	}

	public int getMaximalAbsoluteFrequency() {
		int maxFreq = frequencies[0];
		for (int i = 1; i < frequencies.length; i++)
			maxFreq = Math.max(frequencies[i], maxFreq);
		return maxFreq;
	}

}
