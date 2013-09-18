package org.openlca.core.matrix;

import java.util.HashMap;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;
import org.openlca.expressions.FormulaInterpreter;

public class ExchangeMatrix {

	private int columns;
	private int rows;
	private final HashMap<Integer, HashMap<Integer, ExchangeCell>> cells;

	public ExchangeMatrix(int rows, int columns) {
		this.columns = columns;
		this.rows = rows;
		cells = new HashMap<>();
	}

	public boolean isEmpty() {
		return cells.isEmpty();
	}

	public void setEntry(int row, int col, ExchangeCell cell) {
		HashMap<Integer, ExchangeCell> rowMap = cells.get(row);
		if (rowMap == null) {
			rowMap = new HashMap<>();
			cells.put(row, rowMap);
		}
		rowMap.put(col, cell);
	}

	public ExchangeCell getEntry(int row, int col) {
		HashMap<Integer, ExchangeCell> rowMap = cells.get(row);
		if (rowMap == null)
			return null;
		return rowMap.get(col);
	}

	public IMatrix createRealMatrix() {
		final IMatrix matrix = MatrixFactory.create(rows, columns);
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ExchangeCell cell) {
				matrix.setEntry(row, col, cell.getMatrixValue());
			}
		});
		return matrix;
	}

	public void eval(final FormulaInterpreter interpreter) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ExchangeCell cell) {
				cell.eval(interpreter);
			}
		});
	}

	public void apply(final IMatrix matrix) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ExchangeCell cell) {
				matrix.setEntry(row, col, cell.getMatrixValue());
			}
		});
	}

	public void simulate(final IMatrix matrix) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ExchangeCell cell) {
				matrix.setEntry(row, col, cell.getNextSimulationValue());
			}
		});
	}

	private void iterate(Fn fn) {
		for (Integer row : cells.keySet()) {
			if (row == null)
				continue;
			HashMap<Integer, ExchangeCell> rowMap = cells.get(row);
			if (rowMap == null)
				continue;
			for (Integer col : rowMap.keySet()) {
				if (col == null)
					continue;
				ExchangeCell cell = rowMap.get(col);
				if (cell == null)
					continue;
				fn.apply(row, col, cell);
			}
		}
	}

	private interface Fn {
		void apply(int row, int col, ExchangeCell cell);
	}

}
