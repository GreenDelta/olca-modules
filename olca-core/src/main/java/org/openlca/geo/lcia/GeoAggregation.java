package org.openlca.geo.lcia;

/**
 * Defines how the values of a numeric property of set of geometric features
 * are aggregated.
 */
public enum GeoAggregation {

	WEIGHTED_AVERAGE,

	AVERAGE,

	MINIMUM,

	MAXIMUM;

	@Override
	public String toString() {
		return switch (this) {
			case WEIGHTED_AVERAGE -> "Weighted average";
			case AVERAGE -> "Average";
			case MINIMUM -> "Minimum";
			case MAXIMUM -> "Maximum";
		};
	}
}
