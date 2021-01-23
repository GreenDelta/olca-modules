package org.openlca.core.math.data_quality;

class Accumulator {

	private final AggregationType aggType;
	private final boolean ceiling;
	private final boolean zeroToMax;
	private final byte max;

	private byte accMax;
	private double accSum;
	private double accTotalWeight;

	Accumulator(DQCalculationSetup setup, byte max) {
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
	void add(byte dq, double weight) {
		if (aggType == AggregationType.MAXIMUM) {
			if (dq == 0) {
				if (zeroToMax) {
					accMax = max;
				}
				return;
			}
			accMax = max(accMax, dq);
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

	void addAll(byte[] dqs, double[] weights) {
		if (aggType == AggregationType.MAXIMUM) {
			for (byte dq : dqs) {
				add(dq, 0);
			}
			return;
		}
		for (int i = 0; i < dqs.length; i++) {
			add(dqs[i], weights[i]);
		}
	}

	/**
	 * Get the accumulated score of the scores that where added before.
	 */
	byte get() {
		if (aggType == AggregationType.MAXIMUM) {
			return (byte) Math.min(accMax, max);
		}
		if (accTotalWeight == 0)
			return 0;
		double value = accSum / accTotalWeight;
		byte accDQ = ceiling
			? (byte) Math.round(Math.ceil(value))
			: (byte) Math.round(value);
		return min(accDQ, max);
	}

	/**
	 * Get the accumulated score of the given scores and weights (if applicable)
	 * without adding them to this accumulator.
	 */
	byte get(byte[] dqs, double[] weights) {
		if (aggType == null || aggType == AggregationType.NONE)
			return 0;

		if (aggType == AggregationType.MAXIMUM) {
			byte m = 0;
			for (byte dq : dqs) {
				if (dq == 0 && zeroToMax) {
					return max;
				}
				m = max(m, dq);
			}
			return min(m, max);
		}

		boolean square = aggType == AggregationType.WEIGHTED_SQUARED_AVERAGE;
		double totalWeight = 0;
		double value = 0;

		for (int i = 0; i < dqs.length; i++) {
			byte dq = dqs[i];
			if (dq == 0) {
				if (!zeroToMax)
					continue;
				dq = max;
			}

			double weight = square
				? Math.pow(weights[i], 2)
				: Math.abs(weights[i]);
			totalWeight += weight;
			value += dq * weight;
		}

		if (totalWeight == 0)
			return 0;
		value /= totalWeight;
		byte m = ceiling
			? (byte) Math.round(Math.ceil(value))
			: (byte) Math.round(value);
		return min(m, max);
	}

	private static byte min(byte b1, byte b2) {
		return b1 > b2 ? b2 : b1;
	}

	private static byte max(byte b1, byte b2) {
		return b1 < b2 ? b2 : b1;
	}
}
