package org.openlca.core.matrix.format;

/**
 * An interface for matrices of (signed) byte values.
 */
public interface ByteMatrix extends ByteMatrixReader {

	void set(int row, int col, byte value);

}
