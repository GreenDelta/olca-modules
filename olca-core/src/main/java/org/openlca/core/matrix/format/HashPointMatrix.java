package org.openlca.core.matrix.format;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * A sparse matrix implementation that uses primitive hash maps from the Trove
 * project to store the data. Filling this matrix is fast with relatively low
 * memory consumption.
 *
 * Note that you have to make sure to set the respective row and column size
 * when there are empty rows or columns.
 */
public class HashPointMatrix implements IMatrix {

	public int rows;
	public int cols;

	private final TIntObjectHashMap<TIntDoubleHashMap> data;

	public HashPointMatrix() {
		data = new TIntObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
	}

	public HashPointMatrix(int rows, int cols) {
		this();
		this.rows = rows;
		this.cols = cols;
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

		// ensure matrix size
		if (row >= rows) {
			rows = row + 1;
		}
		if (col >= cols) {
			cols = col + 1;
		}

		var rowMap = data.get(row);
		if (rowMap == null) {
			rowMap = new TIntDoubleHashMap(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1,
					0);
			data.put(row, rowMap);
		}
		rowMap.put(col, val);
	}

	private boolean hasEntry(int row, int col) {
		var rowMap = data.get(row);
		if (rowMap == null)
			return false;
		return rowMap.get(col) != 0;
	}

	public void clear() {
		data.clear();
		rows = 0;
		cols = 0;
	}

	@Override
	public double get(int row, int col) {
		var rowMap = data.get(row);
		if (rowMap == null)
			return 0;
		return rowMap.get(col);
	}

	@Override
	public double[] getColumn(int col) {
		double[] column = new double[rows];
		var iter = data.iterator();
		while (iter.hasNext()) {
			iter.advance();
			var row = iter.key();
			column[row] =  iter.value().get(col);
		}
		return column;
	}

	@Override
	public double[] getRow(int i) {
		double[] row = new double[cols];
		var values = data.get(i);
		if (values == null)
			return row;
		var iter = values.iterator();
		while(iter.hasNext()) {
			iter.advance();
			var col = iter.key();
			row[col] = iter.value();
		}
		return row;
	}

	@Override
	public HashPointMatrix copy() {
		var copy = new HashPointMatrix();
		copy.rows = rows;
		copy.cols = cols;
		iterate(copy::set);
		return copy;
	}

	@Override
	public void iterate(EntryFunction fn) {
		var rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			int row = rows.key();
			var cols = rows.value().iterator();
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
		iterate((row, col, val) -> x[row] += val * v[col]);
		return x;
	}

	/**
	 * Scales each column j of the matrix with the value v[j] of the given
	 * vector: M * diagm(v). The matrix is modified in-place.
	 */
	public void scaleColumns(double[] v) {
		var rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			var cols = rows.value().iterator();
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
		int maxRows = Math.min(rows, 15);
		int maxCols = Math.min(cols, 15);
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
