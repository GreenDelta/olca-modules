package org.openlca.geo.parameter;

class Val {

	/**
	 * Shapefiles do not support NaN values of attributes. We define NaN to be a
	 * value that is smaller or equal to -9_999_999_999d
	 */
	static boolean isNaN(double value) {
		return value <= -9_999_999_999d;
	}

}
