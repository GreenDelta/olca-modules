package org.openlca.core.math;

public interface IMatrixFactory<M extends IMatrix> {

	/**
	 * Creates a matrix of the given size.
	 * 
	 * @param rows
	 *            the number of rows of the matrix.
	 * @param columns
	 *            the number of columns of the matrix.
	 */
	M create(int rows, int columns);

}
