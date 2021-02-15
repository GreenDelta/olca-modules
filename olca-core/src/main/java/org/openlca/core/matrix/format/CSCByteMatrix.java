package org.openlca.core.matrix.format;

public class CSCByteMatrix implements ByteMatrixReader {

	public final int rows;
	public final int columns;
	public final byte[] values;
	public final int[] columnPointers;
	public final int[] rowIndices;

	public CSCByteMatrix(
		int rows,
		int columns,
		byte[] values,
		int[] columnPointers,
		int[] rowIndices) {
		this.rows = rows;
		this.columns = columns;
		this.values = values;
		this.columnPointers = columnPointers;
		this.rowIndices = rowIndices;
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
	public byte get(int row, int col) {
		int idxStart = columnPointers[col];
		int idxEnd = col == (columns - 1)
			? rowIndices.length
			: columnPointers[col + 1];
		for (int idx = idxStart; idx < idxEnd; idx++) {
			int r = rowIndices[idx];
			if (r == row)
				return values[idx];
		}
		return 0;
	}

	@Override
	public byte[] getColumn(int j) {
		byte[] v = new byte[rows];
		int idxStart = columnPointers[j];
		int idxEnd = j == (columns - 1)
			? rowIndices.length
			: columnPointers[j + 1];
		for (int idx = idxStart; idx < idxEnd; idx++) {
			int r = rowIndices[idx];
			v[r] = values[idx];
		}
		return v;
	}

	@Override
	public byte[] getRow(int i) {
		byte[] v = new byte[columns];
		for (int col = 0; col < columns; col++) {
			v[col] = get(i, col);
		}
		return v;
	}

	@Override
	public void iterate(ByteEntryFunction fn) {
		for (int col = 0; col < columns; col++) {
			int start = columnPointers[col];
			int end = col < (columns - 1)
				? columnPointers[col + 1]
				: values.length;
			for (int i = start; i < end; i++) {
				int row = rowIndices[i];
				byte val = values[i];
				if (val != 0) {
					fn.value(row, col, val);
				}
			}
		}
	}
}
