package org.openlca.core.matrix.format;

/**
 * Interface with the general matrix operations used in openLCA.
 */
public interface Matrix extends MatrixReader {

	/** Set the entry in the given row and column to the given value. */
	void set(int row, int col, double val);

	@Override
	Matrix copy();

	default void setValues(double[][] values) {
		if (values == null)
			return;
		int rows = rows();
		int cols = columns();
		for (int i = 0; i < values.length; i++) {
			double[] row = values[i];
			if (i >= rows || row == null)
				break;
			for (int j = 0; j < row.length; j++) {
				if (j >= cols)
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

}
