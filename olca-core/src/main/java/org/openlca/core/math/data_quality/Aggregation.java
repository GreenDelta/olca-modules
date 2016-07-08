package org.openlca.core.math.data_quality;

import java.util.List;

class Aggregation {

	public static int[] applyTo(List<AggregationValue> values, AggregationType type) {
		if (values.isEmpty())
			return null;
		switch (type) {
		case WEIGHTED_AVERAGE:
			return weightedAverageOf(values, false);
		case WEIGHTED_SQUARED_AVERAGE:
			return weightedAverageOf(values, true);
		case MAXIMUM:
			return maximumOf(values);
		default:
			return null;
		}
	}

	private static int[] weightedAverageOf(List<AggregationValue> values, boolean squared) {
		double[] aggregated = new double[values.get(0).values.length];
		double[] divisors = new double[values.get(0).values.length];
		for (AggregationValue value : values) {
			for (int i = 0; i < value.values.length; i++) {
				if (value.values[i] == 0)
					continue;
				double factor = value.factor;
				if (squared) {
					factor = Math.pow(factor, 2);
				}
				aggregated[i] += value.values[i] * factor;
				divisors[i] += value.factor;
			}
		}
		int[] result = new int[aggregated.length];
		for (int i = 0; i < aggregated.length; i++) {
			if (aggregated[i] == 0 || divisors[i] == 0)
				continue;
			double value = aggregated[i] / divisors[i];
			if (squared) {
				value = Math.sqrt(value);
			}
			result[i] = (int) Math.round(value);
		}
		return result;
	}

	private static int[] maximumOf(List<AggregationValue> values) {
		int[] result = new int[values.get(0).values.length];
		for (AggregationValue value : values) {
			for (int i = 0; i < value.values.length; i++) {
				result[i] = Math.max(result[i], value.values[i]);
			}
		}
		return result;
	}

	static class AggregationValue {

		private final int[] values;
		private final double factor;

		AggregationValue(int[] values, double factor) {
			this.values = values;
			this.factor = factor;
		}

	}

}
