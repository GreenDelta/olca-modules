package org.openlca.core.matrix.format;

public interface ByteMatrixReader {

	int rows();

	int columns();

	byte get(int row, int col);

	byte[] getColumn(int col);

	byte[] getRow(int row);
}
