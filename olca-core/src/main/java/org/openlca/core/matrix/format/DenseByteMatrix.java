package org.openlca.core.matrix.format;

import java.util.Arrays;

/**
 * An dense matrix that stores its values in a plain byte array in column major
 * order.
 */
public class DenseByteMatrix implements ByteMatrix {

	public final int rows;
	public final int columns;
	public final byte[] data;

	public DenseByteMatrix(int rows, int columns) {
		this(rows, columns, new byte[rows * columns]);
	}

	public DenseByteMatrix(int rows, int columns, byte[] data) {
		this.rows = rows;
		this.columns = columns;
		this.data = data;
	}

	public static DenseByteMatrix of(ByteMatrixReader other) {
		if (other instanceof DenseByteMatrix) {
			var otherData = ((DenseByteMatrix) other).data;
			return new DenseByteMatrix(other.rows(), other.columns(),
				Arrays.copyOf(otherData, otherData.length));
		}
		var dense = new DenseByteMatrix(other.rows(), other.columns());
		other.iterate(dense::set);
		return dense;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public void set(int row, int col, byte value) {
		data[index(row, col)] = value;
	}

	@Override
	public byte get(int row, int col) {
		return data[index(row, col)];
	}

	@Override
	public byte[] getColumn(int j) {
		byte[] col = new byte[rows];
		int start = index(0, j);
		if (rows >= 0) {
			System.arraycopy(data, start, col, 0, rows);
		}
		return col;
	}

	@Override
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
