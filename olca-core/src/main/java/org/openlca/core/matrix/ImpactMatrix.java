package org.openlca.core.matrix;

import java.util.HashMap;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.MatrixFactory;

/**
 * A matrix with impact assessment factors where the flows are mapped to the
 * columns and the impact categories are mapped to the rows. The factors should
 * be negative in this matrix if the corresponding flow is an input flow.
 */
public class ImpactMatrix {

	private int columns;
	private int rows;
	private final HashMap<Integer, HashMap<Integer, ImpactFactorCell>> cells;

	public ImpactMatrix(int rows, int columns) {
		this.columns = columns;
		this.rows = rows;
		cells = new HashMap<>();
	}

	public boolean isEmpty() {
		return cells.isEmpty();
	}

	public void setEntry(int row, int col, ImpactFactorCell cell) {
		HashMap<Integer, ImpactFactorCell> rowMap = cells.get(row);
		if (rowMap == null) {
			rowMap = new HashMap<>();
			cells.put(row, rowMap);
		}
		rowMap.put(col, cell);
	}

	public ImpactFactorCell getEntry(int row, int col) {
		HashMap<Integer, ImpactFactorCell> rowMap = cells.get(row);
		if (rowMap == null)
			return null;
		return rowMap.get(col);
	}

	public IMatrix createRealMatrix() {
		final IMatrix matrix = MatrixFactory.create(rows, columns);
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				matrix.setEntry(row, col, cell.getMatrixValue());
			}
		});
		return matrix;
	}

	public void apply(final IMatrix matrix) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				matrix.setEntry(row, col, cell.getMatrixValue());
			}
		});
	}

	public void simulate(final IMatrix matrix) {
		iterate(new Fn() {
			@Override
			public void apply(int row, int col, ImpactFactorCell cell) {
				matrix.setEntry(row, col, cell.getNextSimulationValue());
			}
		});
	}

	private void iterate(Fn fn) {
		for (Integer row : cells.keySet()) {
			if (row == null)
				continue;
			HashMap<Integer, ImpactFactorCell> rowMap = cells.get(row);
			if (rowMap == null)
				continue;
			for (Integer col : rowMap.keySet()) {
				if (col == null)
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
