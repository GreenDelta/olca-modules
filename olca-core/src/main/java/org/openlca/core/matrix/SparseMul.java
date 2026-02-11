package org.openlca.core.matrix;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.SparseMatrixReader;

/**
 * Utility class for efficient sparse matrix multiplication.
 * Supports {@link HashPointMatrix} and {@link CSCMatrix} types.
 */
public final class SparseMul {

	private SparseMul() {
	}

	/**
	 * Multiplies two matrices efficiently by iterating over the non-zero entries
	 * of B and adding the scaled columns of A to the result: C[:, j] += B[k, j] * A[:, k].
	 *
	 * @param a the left matrix
	 * @param b the right matrix
	 * @return the result matrix C = A * B
	 */
	public static HashPointMatrix multiply(SparseMatrixReader a, SparseMatrixReader b) {
		if (a.columns() != b.rows()) {
			throw new IllegalArgumentException(
					"Dimensions mismatch: " + a.columns() + " columns vs " + b.rows() + " rows");
		}

		var result = new HashPointMatrix(a.rows(), b.columns());
		var cscA = a.pack();

		// Drive multiplication by the non-zero entries of B
		b.iterate((k, j, bVal) -> {
			if (bVal == 0)
				return;

			// Add bVal * A[:,k] to result[:,j]
			int start = cscA.columnPointers[k];
			int end = cscA.columnPointers[k + 1];
			for (int i = start; i < end; i++) {
				int row = cscA.rowIndices[i];
				double aVal = cscA.values[i];
				result.add(row, j, aVal * bVal);
			}
		});

		return result;
	}

}
