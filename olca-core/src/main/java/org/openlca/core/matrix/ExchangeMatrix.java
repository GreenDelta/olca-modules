package org.openlca.core.matrix;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.expressions.FormulaInterpreter;

public class ExchangeMatrix {

	private int columns;
	private int rows;
	private final TIntObjectHashMap<TIntObjectHashMap<ExchangeCell>> cells;

	public ExchangeMatrix(int rows, int columns) {
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

	public void setEntry(int row, int col, ExchangeCell cell) {
		TIntObjectHashMap<ExchangeCell> rowMap = cells.get(row);
		if (rowMap == null) {
			rowMap = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR, -1);
			cells.put(row, rowMap);
		}
		rowMap.put(col, cell);
	}

	public ExchangeCell getEntry(int row, int col) {
		TIntObjectHashMap<ExchangeCell> rowMap = cells.get(row);
		if (rowMap == null)
			return null;
		return rowMap.get(col);
	}

	public <M extends IMatrix> M createRealMatrix(IMatrixFactory<M> factory) {
		final M matrix = factory.create(rows, columns);
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

	private interface Fn {
		void apply(int row, int col, ExchangeCell cell);
	}

}
