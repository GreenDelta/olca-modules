package org.openlca.core.matrix;

import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * A matrix with impact assessment factors where the flows are mapped to the
 * columns and the impact categories are mapped to the rows. The factors should
 * be negative in this matrix if the corresponding flow is an input flow.
 */
public class ImpactFactorMatrix {

	private final int columns;
	private final int rows;
	private final TIntObjectHashMap<TIntObjectHashMap<ImpactFactorCell>> cells;

	public ImpactFactorMatrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		cells = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
	}

	public boolean isEmpty() {
		return cells.isEmpty();
	}

	public void setEntry(int row, int col, ImpactFactorCell cell) {
		TIntObjectHashMap<ImpactFactorCell> rowMap = cells.get(row);
		if (rowMap == null) {
			rowMap = new TIntObjectHashMap<>(Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR, -1);
			cells.put(row, rowMap);
		}
		rowMap.put(col, cell);
	}

	public ImpactFactorCell getEntry(int row, int col) {
		TIntObjectHashMap<ImpactFactorCell> rowMap = cells.get(row);
		if (rowMap == null)
			return null;
		return rowMap.get(col);
	}

	public IMatrix createRealMatrix(IMatrixSolver solver) {
		IMatrix matrix = solver.matrix(rows, columns);
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				matrix.set(row, col, cell.getMatrixValue());
			}
		});
		return matrix;
	}

	void eval(final FormulaInterpreter interpreter) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				cell.eval(interpreter);
			}
		});
	}

	public void apply(final IMatrix matrix) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				matrix.set(row, col, cell.getMatrixValue());
			}
		});
	}

	public void simulate(final IMatrix matrix) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				matrix.set(row, col, cell.getNextSimulationValue());
			}
		});
	}

	private void iterate(Fn fn) {
		for (int row : cells.keys()) {
			if (row == -1)
				continue;
			TIntObjectHashMap<ImpactFactorCell> rowMap = cells.get(row);
			if (rowMap == null)
				continue;
			for (int col : rowMap.keys()) {
				if (col == -1)
					continue;
				ImpactFactorCell cell = rowMap.get(col);
				if (cell == null)
					continue;
				fn.apply(row, col, cell);
			}
		}
	}

	private interface Fn {
		void apply(int row, int col, ImpactFactorCell cell);
	}

}
