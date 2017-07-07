package org.openlca.core.matrix.format;

/**
 * An implementation that uses a double precision array to store the data. Data
 * are stored in column-major order.
 */
public class DenseMatrix implements IMatrix {

	private final double[] data;
	private final int rows;
	private final int columns;

	public DenseMatrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		data = new double[rows * columns];
	}

	public double[] getData() {
		return data;
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
	public void set(int row, int col, double val) {
		data[index(row, col)] = val;
	}

	@Override
	public double get(int row, int col) {
		return data[index(row, col)];
	}

	@Override
	public double[] getColumn(int i) {
		int start = index(0, i);
		double[] col = new double[rows];
		System.arraycopy(data, start, col, 0, rows);
		return col;
	}

	@Override
	public double[] getRow(int i) {
		double[] row = new double[columns];
		for (int c = 0; c < columns; c++) {
			row[c] = get(i, c);
		}
		return row;
	}

	private int index(int row, int column) {
		return row + rows * column;
	}

	@Override
	public DenseMatrix copy() {
		DenseMatrix copy = new DenseMatrix(rows, columns);
		System.arraycopy(data, 0, copy.data, 0, data.length);
		return copy;
	}

}
