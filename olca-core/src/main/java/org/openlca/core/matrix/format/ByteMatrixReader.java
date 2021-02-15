package org.openlca.core.matrix.format;

public interface ByteMatrixReader {

	int rows();

	int columns();

	byte get(int row, int col);

	byte[] getColumn(int col);

	byte[] getRow(int row);

	default void iterate(ByteEntryFunction fn) {
		if (fn == null)
			return;
		for (int col = 0; col < columns(); col++) {
			for (int row = 0; row < rows(); row++) {
				byte val = get(row, col);
				if (val != 0) {
					fn.value(row, col, val);
				}
			}
		}
	}
}
