package org.openlca.core.matrix.format;

/**
 * An dense matrix that stores its values in a plain byte array in column major
 * order.
 */
public class DenseByteMatrix {

	public final int rows;
	public final int columns;
	private final byte[] data;

	public DenseByteMatrix(int rows, int columns) {
		this(rows, columns, new byte[rows * columns]);
	}

	public DenseByteMatrix(int rows, int columns, byte[] data) {
		this.rows = rows;
		this.columns = columns;
		this.data = data;
	}

	public void set(int row, int col, byte value) {
		data[index(row, col)] = value;
	}

	public byte get(int row, int col) {
		return data[index(row, col)];
	}

	public byte[] getColumn(int j) {
		byte[] col = new byte[rows];
		int start = index(0, j);
		if (rows >= 0) {
			System.arraycopy(data, start, col, 0, rows);
		}
		return col;
	}

	public byte[] getRow(int i) {
		byte[] row = new byte[columns];
		for (int j = 0; j < columns; j++) {
			row[j] = get(i, j);
		}
		return row;
	}

	private int index(int row, int column) {
		return row + rows * column;
	}
}
