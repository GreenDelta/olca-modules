package org.openlca.geo.lcia;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * Describes the calculated value of a property for a location based on the
 * intersections of that location with the geometries of the property features.
 */
record PropVal(GeoProperty param, double value) {

	static PropVal of(GeoProperty param, double value) {
		return new PropVal(param, value);
	}

	/**
	 * Takes the default value of the given property.
	 */
	static PropVal defaultOf(GeoProperty p) {
		return new PropVal(p, p.defaultValue);
	}

	/**
	 * Calculates the aggregated value for the given property based on the
	 * property values and corresponding intersection shares. The aggregation
	 * function as defined in the property is used to calculate the aggregated
	 * value.
	 */
	static PropVal of(GeoProperty param, List<Double> vals, List<Double> shares) {
		if (vals.isEmpty())
			return PropVal.defaultOf(param);
		if (param.aggregation == null)
			return PropVal.of(param, weightedMean(vals, shares));

		double value = switch (param.aggregation) {
			case MINIMUM -> reduce(Math::min, vals);
			case MAXIMUM -> reduce(Math::max, vals);
			case AVERAGE -> mean(vals);
			case WEIGHTED_AVERAGE -> weightedMean(vals, shares);
		};
		return PropVal.of(param, value);
	}

	private static double reduce(DoubleBinaryOperator fn, List<Double> vals) {
		double val = vals.get(0) == null ? 0 : vals.get(0);
		for (int i = 1; i < vals.size(); i++) {
			var next = vals.get(i);
			if (next == null)
				continue;
			val = fn.applyAsDouble(val, next);
		}
		return val;
	}

	private static double mean(List<Double> vals) {
		double sum = 0;
		int count = 0;
		for (Double next : vals) {
			if (next == null)
				continue;
			sum += next;
			count++;
		}
		return count > 0
				? sum / count
				: 0;
	}

	private static double weightedMean(List<Double> vals, List<Double> shares) {
		double sum = 0;
		double wsum = 0;
		for (int i = 0; i < vals.size(); i++) {
			Double next = vals.get(i);
			Double share = shares.get(i);
			if (next == null || share == null)
				continue;
			sum += next * share;
			wsum += share;
		}
		return wsum == 0 ? 0 : sum / wsum;
	}
}
