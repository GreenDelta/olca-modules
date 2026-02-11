package org.openlca.core.matrix.format;

/// An interface for sparse matrices that can be converted between a compressed
/// (packed) and a hash-indexed (unpacked) format.
public sealed interface SparseMatrixReader extends MatrixReader
		permits CscMatrix, HashPointMatrix {

	/// Returns a compressed representation (CSC) of this matrix.
	CscMatrix pack();

	/// Returns an editable, hash-based representation of this matrix.
	HashPointMatrix unpack();

	/// Multiplies this matrix with the given other sparse matrix. This is a
	/// default implementation that runs in pure Java land that could be replaced
	/// in specific solvers.
	default HashPointMatrix multiply(SparseMatrixReader other) {
		if (this.columns() != other.rows()) {
			throw new IllegalArgumentException(
				"Dimensions mismatch: " + this.columns()
				+ " columns vs " + other.rows() + " rows");
		}

		var result = new HashPointMatrix(this.rows(), other.columns());
		var cscA = this.pack();

		// the multiplication is performed by iterating over the non-zero
		// entries of the other matrix B and adding the scaled columns
		// of this matrix A to the result matric C:
	  // C[:, j] += B[k, j] * A[:, k]

		other.iterate((k, j, valB) -> {
			if (valB == 0)
				return;

			// Add B[k,j] * A[:,k] to result[:,j]
			int start = cscA.columnPointers[k];
			int end = cscA.columnPointers[k + 1];
			for (int i = start; i < end; i++) {
				int row = cscA.rowIndices[i];
				double valA = cscA.values[i];
				result.add(row, j, valB * valA);
			}
		});

		return result;
	}

}
