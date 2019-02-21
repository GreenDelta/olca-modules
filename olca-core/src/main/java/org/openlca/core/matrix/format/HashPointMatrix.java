package org.openlca.core.matrix.format;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * A sparse matrix implementation that uses primitive hash maps from the Trove
 * project to store the data. Filling this matrix is fast with relatively low
 * memory consumption.
 */
public class HashPointMatrix implements IMatrix {

	private final int rows;
	private final int cols;

	private final TIntObjectHashMap<TIntDoubleHashMap> data;

	public HashPointMatrix(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		data = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
	}

	public HashPointMatrix(double[][] values) {
		rows = values.length;
		int cols = 1;
		for (int row = 0; row < rows; row++)
			cols = Math.max(cols, values[row].length);
		this.cols = cols;
		data = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		for (int row = 0; row < rows; row++) {
			double[] rowVals = values[row];
			for (int col = 0; col < rowVals.length; col++)
				set(row, col, rowVals[col]);
		}
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int columns() {
		return cols;
	}

	@Override
	public void set(int row, int col, double val) {
		// do nothing if val = 0 *and* when there is no value to overwrite
		if (val == 0 && !hasEntry(row, col))
			return;
		TIntDoubleHashMap rowMap = data.get(row);
		if (rowMap == null) {
			rowMap = new TIntDoubleHashMap(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR, -1, 0);
			data.put(row, rowMap);
		}
		rowMap.put(col, val);
	}

	private boolean hasEntry(int row, int col) {
		TIntDoubleHashMap rowMap = data.get(row);
		if (rowMap == null)
			return false;
		return rowMap.get(col) != 0;
	}

	@Override
	public double get(int row, int col) {
		TIntDoubleHashMap rowMap = data.get(row);
		if (rowMap == null)
			return 0;
		return rowMap.get(col);
	}

	@Override
	public double[] getColumn(int i) {
		double[] column = new double[rows];
		for (int row = 0; row < rows; row++) {
			column[row] = get(row, i);
		}
		return column;
	}

	@Override
	public double[] getRow(int i) {
		double[] row = new double[cols];
		for (int col = 0; col < cols; col++) {
			row[col] = get(i, col);
		}
		return row;
	}

	@Override
	public HashPointMatrix copy() {
		HashPointMatrix copy = new HashPointMatrix(rows, cols);
		iterate((row, col, val) -> copy.set(row, col, val));
		return copy;
	}

	/**
	 * Iterates over the non-zero values in this matrix.
	 */
	public void iterate(EntryFunction fn) {
		TIntObjectIterator<TIntDoubleHashMap> rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			int row = rows.key();
			TIntDoubleIterator cols = rows.value().iterator();
			while (cols.hasNext()) {
				cols.advance();
				fn.value(row, cols.key(), cols.value());
			}
		}
	}

	/**
	 * Performs a matrix-vector multiplication with the given vector v.
	 */
	public double[] multiply(double[] v) {
		double[] x = new double[rows()];
		iterate((row, col, val) -> {
			x[row] += val * v[col];
		});
		return x;
	}

	/**
	 * Scales each column j of the matrix with the value v[j] of the given
	 * vector: M * diagm(v). The matrix is modified in-place.
	 */
	public void scaleColumns(double[] v) {
		TIntObjectIterator<TIntDoubleHashMap> rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			TIntDoubleIterator cols = rows.value().iterator();
			while (cols.hasNext()) {
				cols.advance();
				int col = cols.key();
				cols.setValue(cols.value() * v[col]);
			}
		}
	}

	public CompressedRowMatrix compress() {
		CompressedRowMatrix c = new CompressedRowMatrix(rows, cols);
		int entryCount = getNumberOfEntries();
		c.columnIndices = new int[entryCount];
		c.values = new double[entryCount];
		int idx = 0;
		for (int row = 0; row < rows; row++) {
			c.rowPointers[row] = idx;
			TIntDoubleHashMap rowMap = data.get(row);
			if (rowMap == null)
				continue;
			TIntDoubleIterator it = rowMap.iterator();
			while (it.hasNext()) {
				it.advance();
				c.columnIndices[idx] = it.key();
				c.values[idx] = it.value();
				idx++;
			}
		}
		return c;
	}

	public int getNumberOfEntries() {
		int entryCount = 0;
		for (int row = 0; row < rows; row++) {
			TIntDoubleHashMap rowMap = data.get(row);
			if (rowMap == null)
				continue;
			entryCount += rowMap.size();
		}
		return entryCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("HashMapMatrix = [");
		int maxRows = rows > 15 ? 15 : rows;
		int maxCols = cols > 15 ? 15 : cols;
		for (int row = 0; row < maxRows; row++) {
			for (int col = 0; col < maxCols; col++) {
				double val = get(row, col);
				builder.append(val);
				if (col < (maxCols - 1))
					builder.append(",");
			}
			if (row < (maxRows - 1))
				builder.append(";");
		}
		if (maxCols < cols)
			builder.append(", ...");
		if (maxRows < rows)
			builder.append("; ...");
		builder.append("]");
		return builder.toString();
	}

}
