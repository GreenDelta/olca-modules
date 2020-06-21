package org.openlca.core.math.data_quality;

import java.util.function.Supplier;

class Accumulator {

	private final AggregationType aggType;
	private final boolean ceiling;
	private final boolean zeroToMax;
	private final int max;

	private int accMax;
	private double accSum;
	private double accTotalWeight;

	Accumulator(DQCalculationSetup setup, int max) {
		this.aggType = setup.aggregationType;
		this.ceiling = setup.ceiling;
		this.zeroToMax = setup.naHandling == NAHandling.USE_MAX;
		this.max = max;
	}

	/**
	 * Resets the accumulator so that it can be reused.
	 */
	void reset() {
		accMax = 0;
		accSum = 0;
		accTotalWeight = 0;
	}

	/**
	 * Adds the given score with the given weight (if applicable) to this
	 * accumulator.
	 */
	void add(int dq, double weight) {
		if (aggType == AggregationType.MAXIMUM) {
			if (dq == 0) {
				if (zeroToMax) {
					accMax = max;
				}
				return;
			}
			accMax = Math.max(accMax, dq);
			return;
		}

		double _dq = dq;
		if (dq == 0) {
			if (zeroToMax) {
				_dq = max;
			} else {
				return;
			}
		}
		double w = aggType == AggregationType.WEIGHTED_SQUARED_AVERAGE
				? Math.pow(weight, 2)
				: Math.abs(weight);
		accSum += _dq * w;
		accTotalWeight += w;
	}

	/**
	 * Get the accumulated score of the scores that where added before.
	 */
	int get() {
		if (aggType == AggregationType.MAXIMUM) {
			return Math.min(accMax, max);
		}
		if (accTotalWeight == 0)
			return 0;
		double value = accSum / accTotalWeight;
		int accDQ = ceiling
				? Math.round((float) Math.ceil(value))
				: Math.round((float) value);
		return Math.min(accDQ, max);
	}

	/**
	 * Get the accumulated score of the given scores and weights (if applicable)
	 * without adding them to this accumulator.
	 */
	int get(int[] dqs, Supplier<double[]> weightsFn) {
		if (aggType == null || aggType == AggregationType.NONE)
			return 0;

		if (aggType == AggregationType.MAXIMUM) {
			int m = 0;
			for (int dq : dqs) {
				if (dq == 0 && zeroToMax) {
					return max;
				}
				m = Math.max(m, dq);
			}
			return Math.min(m, max);
		}

		boolean square = aggType == AggregationType.WEIGHTED_SQUARED_AVERAGE;
		double[] weights = weightsFn.get();
		double totalWeight = 0;
		double value = 0;

		for (int i = 0; i < dqs.length; i++) {
			int dq = dqs[i];
			if (dq == 0) {
				if (!zeroToMax)
					continue;
				dq = max;
			}

			double weight = square
					? Math.pow(weights[i], 2)
					: Math.abs(weights[i]);
			totalWeight += weight;
			value += ((double) dq) * weight;
		}

		if (totalWeight == 0)
			return 0;
		value /= totalWeight;
		int m = ceiling
				? Math.round((float) Math.ceil(value))
				: Math.round((float) value);
		return Math.min(m, max);
	}

}
