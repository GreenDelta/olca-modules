package org.openlca.core.matrix;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;

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
	public static Matrix multiply(MatrixReader a, MatrixReader b) {
		if (a.columns() != b.rows()) {
			throw new IllegalArgumentException(
					"Dimensions mismatch: " + a.columns() + " columns vs " + b.rows() + " rows");
		}

		var result = new HashPointMatrix(a.rows(), b.columns());

		// Drive multiplication by the non-zero entries of B
		b.iterate((k, j, bVal) -> {
			if (bVal == 0)
				return;

			if (a instanceof CSCMatrix cscA) {
				// Efficient path for CSC (Column-compressed)
				int start = cscA.columnPointers[k];
				int end = cscA.columnPointers[k + 1];
				for (int i = start; i < end; i++) {
					int row = cscA.rowIndices[i];
					double aVal = cscA.values[i];
					add(result, row, j, aVal * bVal);
				}
			} else {
				// Fallback for other formats (HashPoint, etc.)
				double[] colA = a.getColumn(k);
				for (int i = 0; i < colA.length; i++) {
					double aVal = colA[i];
					if (aVal != 0) {
						add(result, i, j, aVal * bVal);
					}
				}
			}
		});

		return result;
	}

	private static void add(HashPointMatrix m, int row, int col, double val) {
		double current = m.get(row, col);
		m.set(row, col, current + val);
	}
}
