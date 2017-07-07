package org.openlca.core.matrix.format;

/**
 * Interface with the general matrix operations used in openLCA.
 */
public interface IMatrix {

	/** Returns the number of rows of the matrix. */
	int rows();

	/** Returns the number of columns of the matrix. */
	int columns();

	/** Set the entry in the given row and column to the given value. */
	void set(int row, int col, double val);

	/** Get the value of the given row and column. */
	double get(int row, int col);

	/** Get the row values of the given column. */
	double[] getColumn(int i);

	/** Get the column values of the given row. */
	double[] getRow(int i);

	/** Creates a copy of this matrix and returns it */
	IMatrix copy();

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
}
