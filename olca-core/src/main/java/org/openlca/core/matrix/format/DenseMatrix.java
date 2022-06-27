package org.openlca.core.matrix.format;

/**
 * An implementation that uses a double precision array to store the data. Data
 * are stored in column-major order.
 */
public class DenseMatrix implements Matrix {

	public final double[] data;
	public final int rows;
	public final int columns;

	public DenseMatrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		data = new double[rows * columns];
	}

	public DenseMatrix(int rows, int columns, double[] data) {
		this.rows = rows;
		this.columns = columns;
		this.data = data;
	}

	/**
	 * Constructs a new matrix from the given values.
	 *
	 * @param values The matrix values as an array of rows (row-major order).
	 */
	public static DenseMatrix of(double[][] values) {
		var m = new DenseMatrix(values.length, Util.columnsOf(values));
		m.setValues(values);
		return m;
	}

	/**
	 * Constructs a new dense matrix from the given other matrix. The data of
	 * the other matrix are copied into the new matrix.
	 *
	 * @param other the other matrix
	 * @return a new matrix with copied values
	 */
	public static DenseMatrix of(MatrixReader other) {
		if (other instanceof DenseMatrix) {
			return ((DenseMatrix) other).copy();
		}
		var m = new DenseMatrix(other.rows(), other.columns());
		other.iterate(m::set);
		return m;
	}

	@Override
	public final boolean isSparse() {
		return false;
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
	public void setColumn(int col, double[] values) {
		System.arraycopy(values, 0, data, col * rows, values.length);
	}

	@Override
	public void setColumnRange(int col, int offset, double[] values) {
		System.arraycopy(values, 0, data, col * rows + offset, values.length);
	}

	@Override
	public double[] getRow(int i) {
		double[] row = new double[columns];
		for (int c = 0; c < columns; c++) {
			row[c] = get(i, c);
		}
		return row;
	}

	@Override
	public void readColumn(int column, double[] buffer) {
		int n = Math.min(buffer.length, rows);
		System.arraycopy(data, column * rows, buffer, 0, n);
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

	@Override
	public String toString() {
		if (rows > 10 || columns > 10)
			return "DenseMatrix{rows=" + rows + ", columns=" + columns + '}';
		StringBuilder b = new StringBuilder("[");
		for (int row = 0; row < rows; row++) {
			if (row > 0) {
				b.append(" ");
			}
			for (int col = 0; col < columns; col++) {
				double val = get(row, col);
				if (val >= 0.1 && val < 100) {
					b.append(String.format(" %.2f ", val));
				} else {
					b.append(String.format(" %.2e ", val));
				}
			}
			if (row < (rows - 1)) {
				b.append(";\n");
			} else {
				b.append(']');
			}
		}
		return b.toString();
	}
}
