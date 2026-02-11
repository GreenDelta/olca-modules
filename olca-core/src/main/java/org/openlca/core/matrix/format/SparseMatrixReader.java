package org.openlca.core.matrix.format;

/**
 * A sealed interface for sparse matrices that can be converted between
 * a compressed (packed) and a hash-indexed (unpacked) format.
 */
public sealed interface SparseMatrixReader extends MatrixReader
		permits CSCMatrix, HashPointMatrix {

	/**
	 * Returns a compressed representation (CSC) of this matrix.
	 */
	CSCMatrix pack();

	/**
	 * Returns an editable, hash-based representation of this matrix.
	 */
	HashPointMatrix unpack();

}
