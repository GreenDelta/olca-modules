package org.openlca.core.math;

public interface IMatrixFactory {

	/**
	 * Creates a general matrix of the given size.
	 * 
	 * @param rows
	 *            the number of rows of the matrix.
	 * @param columns
	 *            the number of columns of the matrix.
	 */
	IMatrix create(int rows, int columns);

	/**
	 * Creates a sparse matrix of the given size. If there is no sparse matrix
	 * implementation available, this function should return a general matrix of
	 * the given size. Note that all functions of the solver interface must be
	 * supported by the sparse matrix implementation, maybe also in combination
	 * with the general matrix implementation.
	 * 
	 * @param rows
	 *            the number of rows of the matrix.
	 * @param columns
	 *            the number of columns of the matrix.
	 */
	IMatrix createSparse(int rows, int columns);

	/**
	 * Creates a dense or a sparse matrix depending on the given load factor.
	 */
	IMatrix create(int rows, int columns, double loadFactor);

	/**
	 * The solver implementation that supports the functions of the solver
	 * interface for the matrix implementations created by this factory.
	 */
	ISolver getDefaultSolver();

}
