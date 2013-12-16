package org.openlca.core.math;

/**
 * Interface with the general matrix operations used in openLCA.
 */
public interface IMatrix {

	/** Get the number of rows of the matrix. */
	int getRowDimension();

	/** Get the number of columns of the matrix. */
	int getColumnDimension();

	/** Set the entry in the given row and column to the given value. */
	void setEntry(int row, int col, double val);

	/** Get the value of the given row and column. */
	double getEntry(int row, int col);

	/** Get the row values of the given column. */
	double[] getColumn(int i);

	/** Get the column values of the given row. */
	double[] getRow(int i);

	/** Creates a copy of this matrix and returns it */
	IMatrix copy();

}
