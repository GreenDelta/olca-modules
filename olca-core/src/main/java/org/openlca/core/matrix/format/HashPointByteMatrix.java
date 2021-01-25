package org.openlca.core.matrix.format;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class HashPointByteMatrix implements IByteMatrix {

	public int rows;
	public int columns;

	private final TIntObjectHashMap<TIntByteHashMap> data;

	public HashPointByteMatrix() {
		data = new TIntObjectHashMap<>(
			Constants.DEFAULT_CAPACITY,
			Constants.DEFAULT_LOAD_FACTOR,
			-1);
	}

	public HashPointByteMatrix(int rows, int cols) {
		this();
		this.rows = rows;
		this.columns = cols;
	}

	@Override
	public int rows() {
		return rows;
	}

	@Override
	public int columns() {
		return columns;
	}

	public void clear() {
		data.clear();
		rows = 0;
		columns = 0;
	}

	@Override
	public void set(int row, int col, byte val) {
		if (row >= rows) {
			rows = row + 1;
		}
		if (col >= columns) {
			columns = col + 1;
		}

		var column = data.get(col);
		if (column == null) {
			column = new TIntByteHashMap(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1,
				(byte) 0);
			data.put(col, column);
		}

		if (val == 0) {
			column.remove(row);
		} else {
			column.put(row, val);
		}
	}

	@Override
	public byte get(int row, int col) {
		var column = data.get(col);
		return column == null
			? 0
			: column.get(row);
	}

	@Override
	public byte[] getColumn(int j) {
		byte[] column = new byte[rows];
		var v = data.get(j);
		if (v == null)
			return column;
		var iter = v.iterator();
		while(iter.hasNext()) {
			iter.advance();
			column[iter.key()] = iter.value();
		}
		return column;
	}

	@Override
	public byte[] getRow(int i) {
		byte[] row = new byte[columns];
		var iter = data.iterator();
		while(iter.hasNext()) {
			iter.advance();
			var column = iter.value();
			if (column == null)
				continue;
			var val = column.get(i);
			if (val != 0) {
				row[iter.key()] = val;
			}
		}
		return row;
	}
}
