package org.openlca.core.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Indexable;

/**
 * A data structure for strong double values in a flexible table like structure.
 */
public class Table<R extends Indexable, C extends Indexable> {

	private Index<R> rows;
	private Index<C> columns;
	private List<HashMap<Integer, Double>> values;

	public Table(Class<R> rowType, Class<C> columnType) {
		rows = new Index<>(rowType);
		columns = new Index<>(columnType);
		values = new ArrayList<>();
	}

	public R[] getRows() {
		return rows.getItems();
	}

	public C[] getColumns() {
		return columns.getItems();
	}

	public Map<C, Double> getColumnEntries(R row) {
		Map<C, Double> vals = new HashMap<>();
		for (C column : getColumns()) {
			double val = getEntry(row, column);
			vals.put(column, val);
		}
		return vals;
	}

	public Map<R, Double> getRowEntries(C column) {
		Map<R, Double> vals = new HashMap<>();
		for (R row : getRows()) {
			double val = getEntry(row, column);
			vals.put(row, val);
		}
		return vals;
	}

	public void setEntry(R row, C column, double value) {
		if (row == null || column == null)
			return;
		if (!rows.contains(row)) {
			rows.put(row);
			values.add(new HashMap<Integer, Double>());
		}
		int rowIdx = rows.getIndex(row);
		HashMap<Integer, Double> rowValues = values.get(rowIdx);
		if (!columns.contains(column)) {
			columns.put(column);
		}
		Integer col = columns.getIndex(column);
		rowValues.put(col, value);
	}

	public double getEntry(R row, C column) {
		if (row == null || column == null)
			return 0;
		if (!rows.contains(row) || !columns.contains(column))
			return 0;
		int rowIdx = rows.getIndex(row);
		int colIdx = columns.getIndex(column);
		HashMap<Integer, Double> rowValues = values.get(rowIdx);
		Double val = rowValues.get(colIdx);
		return val == null ? 0 : val.doubleValue();
	}
}
