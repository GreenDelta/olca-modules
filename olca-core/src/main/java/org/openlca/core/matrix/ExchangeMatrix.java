package org.openlca.core.matrix;

import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class ExchangeMatrix {

	private int columns;
	private int rows;
	private final TIntObjectHashMap<TIntObjectHashMap<ExchangeCell>> cells;

	ExchangeMatrix(int rows, int columns) {
		this.columns = columns;
		this.rows = rows;
		cells = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
	}

	public boolean isEmpty() {
		return cells.isEmpty();
	}

	/**
	 * Returns <number of elements>/(rows * cols) which is an indicator for the
	 * occupation of the matrix.
	 */
	public double getLoadFactor() {
		TIntObjectIterator<TIntObjectHashMap<ExchangeCell>> it = cells
				.iterator();
		double size = 0;
		while (it.hasNext()) {
			it.advance();
			TIntObjectHashMap<?> map = it.value();
			if (map == null)
				continue;
			size += map.size();
		}
		return size / (rows * columns);
	}

	void setEntry(int row, int col, ExchangeCell cell) {
		TIntObjectHashMap<ExchangeCell> rowMap = cells.get(row);
		if (rowMap == null) {
			rowMap = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR, -1);
			cells.put(row, rowMap);
		}
		rowMap.put(col, cell);
	}

	ExchangeCell getEntry(int row, int col) {
		TIntObjectHashMap<ExchangeCell> rowMap = cells.get(row);
		if (rowMap == null)
			return null;
		return rowMap.get(col);
	}

	public double getValue(int row, int col) {
		ExchangeCell cell = getEntry(row, col);
		if (cell == null)
			return 0;
		else
			return cell.getMatrixValue();
	}

	public IMatrix createRealMatrix(IMatrixSolver solver) {
		if (rows == 0 || columns == 0)
			return null;
		IMatrix matrix = solver.matrix(rows, columns);
		iterate((row, col, cell) -> {
			matrix.set(row, col, cell.getMatrixValue());
		});
		return matrix;
	}

	void eval(FormulaInterpreter interpreter) {
		iterate((row, col, cell) -> {
			cell.eval(interpreter);
		});
	}

	void apply(IMatrix matrix) {
		iterate((row, col, cell) -> {
			matrix.set(row, col, cell.getMatrixValue());
		});
	}

	void simulate(IMatrix matrix) {
		iterate((row, col, cell) -> {
			matrix.set(row, col, cell.getNextSimulationValue());
		});
	}

	void iterate(CellFunction fn) {
		TIntObjectIterator<TIntObjectHashMap<ExchangeCell>> rowIterator = cells
				.iterator();
		while (rowIterator.hasNext()) {
			rowIterator.advance();
			int row = rowIterator.key();
			TIntObjectHashMap<ExchangeCell> rowMap = rowIterator.value();
			if (rowMap == null)
				continue;
			TIntObjectIterator<ExchangeCell> colIterator = rowMap.iterator();
			while (colIterator.hasNext()) {
				colIterator.advance();
				int col = colIterator.key();
				ExchangeCell cell = colIterator.value();
				if (cell == null)
					continue;
				fn.apply(row, col, cell);
			}
		}
	}

	@FunctionalInterface
	interface CellFunction {

		void apply(int row, int col, ExchangeCell cell);

	}

}
