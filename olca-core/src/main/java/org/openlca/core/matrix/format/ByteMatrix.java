package org.openlca.core.matrix.format;

/**
 * An interface for matrices of (signed) byte values.
 */
public interface ByteMatrix {

	int rows();

	int columns();

	void set(int row, int col, byte value);

	byte get(int row, int col);

	byte[] getColumn(int col);

	byte[] getRow(int row);
}
