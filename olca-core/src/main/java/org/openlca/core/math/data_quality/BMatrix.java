package org.openlca.core.math.data_quality;

/**
 * An integer matrix that stores its values in a plain byte array. It is limited
 * to a very small range (basically just 0..125) but this should be more than
 * enough for the DQ calculations. The values are stored in column majore order.
 */
class BMatrix {

	final int rows;
	final int columns;
	private final byte[] data;

	BMatrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.data = new byte[rows * columns];
	}

	void set(int row, int col, int value) {
		data[index(row, col)] = (byte) value;
	}

	int get(int row, int col) {
		return data[index(row, col)];
	}

	int[] getColumn(int j) {
		int[] col = new int[rows];
		int start = index(0, j);
		for (int i = 0; i < rows; i++) {
			col[i] = data[start + i];
		}
		return col;
	}

	int[] getRow(int i) {
		int[] row = new int[columns];
		for (int j = 0; j < columns; j++) {
			row[j] = get(i, j);
		}
		return row;
	}

	private int index(int row, int column) {
		return row + rows * column;
	}
}
