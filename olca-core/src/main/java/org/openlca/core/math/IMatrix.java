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

	/**
	 * Performs a matrix multiplication with this matrix and the given matrix
	 * and returns the result as new matrix.
	 */
	IMatrix multiply(IMatrix with);

	/**
	 * Performs a matrix addition with this matrix and the given matrix
	 * and returns the result as new matrix.
	 */
	IMatrix add(IMatrix toAdd);

	/**
	 * Solves the linear equation system A * X = B, where A is the matrix on
	 * which this method is called, B is the argument of this method, and X is
	 * returned.
	 */
	IMatrix solve(IMatrix b);

	/** Calculates the inverse of this matrix and returns it. */
	IMatrix getInverse();

	/** Creates a copy of this matrix and returns it */
	IMatrix copy();
	
	/**
	 * Performs a matrix subtraction with this matrix and the given matrix
	 * and returns the result as new matrix.
	 */
	IMatrix subtract(IMatrix b);
	
}
