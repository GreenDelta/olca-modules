package org.openlca.core.math.data_quality;

import java.util.List;

class Aggregation {

	public static double[] applyTo(List<AggregationValue> values, AggregationType type) {
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

	private static double[] weightedAverageOf(List<AggregationValue> values, boolean squared) {
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
				divisors[i] += factor;
			}
		}
		double[] result = new double[aggregated.length];
		for (int i = 0; i < aggregated.length; i++) {
			if (aggregated[i] == 0 || divisors[i] == 0)
				continue;
			double value = aggregated[i] / divisors[i];
			result[i] = value;
		}
		return result;
	}

	private static double[] maximumOf(List<AggregationValue> values) {
		double[] result = new double[values.get(0).values.length];
		for (AggregationValue value : values) {
			for (int i = 0; i < value.values.length; i++) {
				result[i] = Math.max(result[i], value.values[i]);
			}
		}
		return result;
	}

	static class AggregationValue {

		private final double[] values;
		private final double factor;

		AggregationValue(double[] values, double factor) {
			this.values = values;
			this.factor = factor;
		}

	}

}
