package org.openlca.core.math.data_quality;

import java.math.BigDecimal;
import java.math.MathContext;
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
		BigDecimal[] aggregated = new BigDecimal[values.get(0).values.length];
		BigDecimal[] divisors = new BigDecimal[values.get(0).values.length];
		for (AggregationValue value : values) {
			for (int i = 0; i < value.values.length; i++) {
				if (value.values[i] == 0)
					continue;
				BigDecimal factor = value.factor;
				if (squared) {
					factor = factor.pow(2);
				}
				BigDecimal result = new BigDecimal(value.values[i]).multiply(factor);
				if (aggregated[i] == null) {
					aggregated[i] = result;
					divisors[i] = factor;
				} else {
					aggregated[i] = aggregated[i].add(result);
					divisors[i] = divisors[i].add(factor);
				}
			}
		}
		double[] result = new double[aggregated.length];
		for (int i = 0; i < aggregated.length; i++) {
			if (aggregated == null || aggregated[i] == null || aggregated[i].doubleValue() == 0d)
				continue;
			if (divisors == null || divisors[i] == null || divisors[i].doubleValue() == 0d)
				continue;
			System.out.println(aggregated[i]);
			System.out.println(divisors[i]);
			result[i] = aggregated[i].divide(divisors[i], MathContext.DECIMAL128).doubleValue();
		}
		return result;
	}
	
	public static void main(String[] args) {
		double dd = 0E-77;
		System.out.println(dd);
		BigDecimal d = new BigDecimal(dd);
		System.out.println(d.equals(BigDecimal.ZERO));
		System.out.println(d);
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
		private final BigDecimal factor;

		AggregationValue(double[] values, BigDecimal factor) {
			this.values = values;
			this.factor = factor;
		}

	}

}
