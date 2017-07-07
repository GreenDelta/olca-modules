package org.openlca.core.matrix.format;

public class CompressedRowMatrix implements IMatrix {

	double[] values;
	int[] columnIndices;
	int[] rowPointers;
	final int rows;
	final int columns;

	public CompressedRowMatrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		rowPointers = new int[rows];
		columnIndices = new int[0];
		values = new double[0];
	}

	@Override
	public double[] getColumn(int col) {
		double[] column = new double[rows];
		if (columnIndices.length == 0)
			return column;
		for (int row = 0; row < rows; row++)
			column[row] = get(row, col);
		return column;
	}

	@Override
	public IMatrix copy() {
		CompressedRowMatrix copy = new CompressedRowMatrix(rows, columns);
		System.arraycopy(this.rowPointers, 0, copy.rowPointers, 0,
				rowPointers.length);
		copy.columnIndices = new int[this.columnIndices.length];
		System.arraycopy(this.columnIndices, 0, copy.columnIndices, 0,
				columnIndices.length);
		copy.values = new double[this.values.length];
		System.arraycopy(this.values, 0, copy.values, 0, values.length);
		return copy;
	}

	@Override
	public int columns() {
		return columns;
	}

	@Override
	public double get(int row, int col) {
		int idxStart = rowPointers[row];
		int idxEnd = row == (rows - 1) ? columnIndices.length
				: rowPointers[row + 1];
		for (int idx = idxStart; idx < idxEnd; idx++) {
			int column = columnIndices[idx];
			if (column == col)
				return values[idx];
		}
		return 0;
	}

	@Override
	public double[] getRow(int row) {
		double[] rowValues = new double[columns];
		int idxStart = rowPointers[row];
		int idxEnd = row == (rows - 1) ? columnIndices.length
				: rowPointers[row + 1];
		for (int idx = idxStart; idx < idxEnd; idx++) {
			int column = columnIndices[idx];
			rowValues[column] = values[idx];
		}
		return rowValues;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public void set(int row, int col, double val) {
		int idxStart = rowPointers[row];
		int idxEnd = row == (rows - 1) ? columnIndices.length
				: rowPointers[row + 1];
		int pos = findIndex(idxStart, idxEnd, col);
		if (pos == -1 && val == 0)
			return;
		if (pos != -1 && val != 0)
			values[pos] = val;
		else if (pos != -1 && val == 0) {
			columnIndices = deletePos(pos, columnIndices);
			values = deletePos(pos, values);
			for (int nextRow = row + 1; nextRow < rows; nextRow++)
				rowPointers[nextRow] -= 1;
		} else {
			pos = idxEnd;
			columnIndices = insert(pos, col, columnIndices);
			values = insert(pos, val, values);
			for (int nextRow = row + 1; nextRow < rows; nextRow++)
				rowPointers[nextRow] += 1;
		}

	}

	private double[] deletePos(int pos, double[] vals) {
		double[] newVals = new double[vals.length - 1];
		System.arraycopy(vals, 0, newVals, 0, pos);
		if ((pos + 1) < vals.length)
			System.arraycopy(vals, pos + 1, newVals, pos, newVals.length - pos);
		return newVals;
	}

	private int[] deletePos(int pos, int[] vals) {
		int[] newVals = new int[vals.length - 1];
		System.arraycopy(vals, 0, newVals, 0, pos);
		if ((pos + 1) < vals.length)
			System.arraycopy(vals, pos + 1, newVals, pos, newVals.length - pos);
		return newVals;
	}

	private double[] insert(int idx, double val, double[] vals) {
		double[] newVals = new double[vals.length + 1];
		System.arraycopy(vals, 0, newVals, 0, idx);
		newVals[idx] = val;
		if ((idx + 1) < vals.length)
			System.arraycopy(vals, idx, newVals, idx + 1, vals.length - idx);
		return newVals;
	}

	private int[] insert(int idx, int val, int[] vals) {
		int[] newVals = new int[vals.length + 1];
		System.arraycopy(vals, 0, newVals, 0, idx);
		newVals[idx] = val;
		if ((idx + 1) < vals.length)
			System.arraycopy(vals, idx, newVals, idx + 1, vals.length - idx);
		return newVals;
	}

	private int findIndex(int idxStart, int idxEnd, int col) {
		int pos = -1;
		for (int idx = idxStart; idx < idxEnd; idx++) {
			int column = columnIndices[idx];
			if (column == col) {
				pos = idx;
				break;
			}
		}
		return pos;
	}

}
