package org.openlca.core.matrix.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * A sparse matrix implementation that uses primitive hash maps from the Trove
 * project to store the data. Filling this matrix is fast with relatively low
 * memory consumption.
 * <p>
 * Note that you have to make sure to set the respective row and column size
 * when there are empty rows or columns.
 */
public class HashPointMatrix implements Matrix {

	public int rows;
	public int cols;

	/**
	 * Data is stored in {column -> {row -> value}} format because it is then
	 * faster to convert this matrix into the compressed column format
	 * that is used in the math libraries.
	 */
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

	/**
	 * Constructs a new matrix from the given values.
	 *
	 * @param values The matrix values as an array of rows (row-major order).
	 */
	public static HashPointMatrix of(double[][] values) {
		var m = new HashPointMatrix(values.length, Util.columnsOf(values));
		m.setValues(values);
		return m;
	}

	@Override
	public final boolean isSparse() {
		return true;
	}

	public boolean isEmpty() {
		return data.isEmpty();
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
		// ensure matrix size
		if (row >= rows) {
			rows = row + 1;
		}
		if (col >= cols) {
			cols = col + 1;
		}

		var column = data.get(col);
		if (column == null) {
			column = new TIntDoubleHashMap(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1,
					0);
			data.put(col, column);
		}
		if (val == 0) {
			column.remove(row);
		} else {
			column.put(row, val);
		}
	}

	public void clear() {
		data.clear();
		rows = 0;
		cols = 0;
	}

	@Override
	public double get(int row, int col) {
		var column = data.get(col);
		if (column == null)
			return 0;
		return column.get(row);
	}

	@Override
	public double[] getColumn(int j) {
		double[] column = new double[rows];
		var m = data.get(j);
		if (m == null)
			return column;
		var iter = m.iterator();
		while (iter.hasNext()) {
			iter.advance();
			column[iter.key()] = iter.value();
		}
		return column;
	}

	@Override
	public double[] getRow(int i) {
		double[] row = new double[cols];
		var iter = data.iterator();
		while (iter.hasNext()) {
			iter.advance();
			var col = iter.key();
			var colData = iter.value();
			if (colData == null)
				continue;
			row[col] = colData.get(i);
		}
		return row;
	}

	@Override
	public void readColumn(int column, double[] buffer) {
		var columnData = data.get(column);
		if (columnData == null) {
			Arrays.fill(buffer, 0);
			return;
		}
		int n = Math.min(buffer.length, rows);
		for (int row = 0; row < n; row++) {
			buffer[row] = columnData.get(row);
		}
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
		var columns = data.iterator();
		while (columns.hasNext()) {
			columns.advance();
			int col = columns.key();
			var colVals = columns.value();
			if (colVals == null)
				continue;
			var iter = colVals.iterator();
			while (iter.hasNext()) {
				iter.advance();
				fn.value(iter.key(), col, iter.value());
			}
		}
	}

	/**
	 * Scales each column j of the matrix with the value v[j] of the given
	 * vector: M * diagm(v). The matrix is modified in-place.
	 */
	@Override
	public void scaleColumns(double[] v) {
		var columns = data.iterator();
		while (columns.hasNext()) {
			columns.advance();
			int col = columns.key();
			var rows = columns.value();
			if (rows == null)
				continue;
			var iter = rows.iterator();
			while (iter.hasNext()) {
				iter.advance();
				iter.setValue(iter.value() * v[col]);
			}
		}
	}

	public CSCMatrix compress() {
		int[] columnPointers = new int[cols + 1];
		int nonZeros = getNumberOfEntries();
		columnPointers[cols] = nonZeros;
		int[] rowIndices = new int[nonZeros];
		double[] values = new double[nonZeros];

		int pos = 0;
		var entries = new ArrayList<CscEntry>();
		for (int col = 0; col < cols; col++) {
			columnPointers[col] = pos;
			var column = data.get(col);
			if (column == null)
				continue;
			var iter = column.iterator();
			entries.clear();
			while (iter.hasNext()) {
				iter.advance();
				entries.add(new CscEntry(
						iter.key(), iter.value()));
			}
			entries.sort(Comparator.comparingInt(e -> e.row));
			for (var entry : entries) {
				rowIndices[pos] = entry.row;
				values[pos] = entry.val;
				pos++;
			}
		}

		return new CSCMatrix(
				rows,
				cols,
				values,
				columnPointers,
				rowIndices
		);
	}

	public int getNumberOfEntries() {
		int entryCount = 0;
		var columns = data.iterator();
		while (columns.hasNext()) {
			columns.advance();
			var column = columns.value();
			entryCount += column.size();
		}
		return entryCount;
	}

	@Override
	public String toString() {
		var builder = new StringBuilder("HashPointMatrix = [");
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

	private record CscEntry(int row, double val) {
	}

}
