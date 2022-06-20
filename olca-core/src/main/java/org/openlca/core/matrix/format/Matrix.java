package org.openlca.core.matrix.format;

/**
 * Interface with the general matrix operations used in openLCA.
 */
public interface Matrix extends MatrixReader {

	/**
	 * Set the entry in the given row and column to the given value.
	 */
	void set(int row, int col, double val);

	@Override
	Matrix copy();

	/**
	 * Fills the matrix with the given values.
	 *
	 * @param values The values as an array of rows (row-major order). The number
	 *               of rows and columns of the given values may be smaller or
	 *               larger than the rows and columns of this matrix; only the
	 *               matching cells are set.
	 */
	default void setValues(double[][] values) {
		if (values == null)
			return;
		int rowCount = rows();
		int colCount = columns();
		for (int i = 0; i < values.length; i++) {
			double[] row = values[i];
			if (i >= rowCount || row == null)
				break;
			for (int j = 0; j < row.length; j++) {
				if (j >= colCount)
					break;
				set(i, j, row[j]);
			}
		}
	}

	/**
	 * Scales the columns of this matrix in-place with the given factors $v$;
	 * this is like $M = M diagm(v)$. Specifically for sparse matrices this
	 * should be implemented in a more efficient way.
	 */
	default void scaleColumns(double[] v) {
		for (int row = 0; row < this.rows(); row++) {
			for (int col = 0; col < this.columns(); col++) {
				this.set(row, col, v[col] * this.get(row, col));
			}
		}
	}

	/**
	 * Set the values of the given row.
	 *
	 * @param row    the index of the row
	 * @param values the new row values
	 */
	default void setRow(int row, double[] values) {
		for (int col = 0; col < values.length; col++) {
			set(row, col, values[col]);
		}
	}

	/**
	 * Set the values of a range of the given row.
	 *
	 * @param row    the index of the row
	 * @param offset the column offset of the range
	 * @param values the values of the range
	 */
	default void setRowRange(int row, int offset, double[] values) {
		for (int i = 0; i < values.length; i++) {
			set(row, offset + i, values[i]);
		}
	}

	/**
	 * Set the values of the given column.
	 *
	 * @param col    the index of the column
	 * @param values the new column values
	 */
	default void setColumn(int col, double[] values) {
		for (int row = 0; row < values.length; row++) {
			set(row, col, values[row]);
		}
	}

	/**
	 * Set the values of a range of the given column.
	 *
	 * @param col    the index of the column
	 * @param offset the row offset of the range
	 * @param values the values of the range
	 */
	default void setColumnRange(int col, int offset, double[] values) {
		for (int i = 0; i < values.length; i++) {
			set(offset + i, col, values[i]);
		}
	}

}
