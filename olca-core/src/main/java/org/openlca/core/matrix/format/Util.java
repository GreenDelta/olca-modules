package org.openlca.core.matrix.format;

class Util {

	private Util() {
	}

	/**
	 * Returns the (maximum) number of columns of the given (row) values.
	 *
	 * @param values the values as an array of rows
	 */
	static int columnsOf(double[][] values) {
		if (values == null)
			return 0;
		int columns = 0;
		for (double[] row : values) {
			columns = Math.max(columns, row.length);
		}
		return columns;
	}
}
