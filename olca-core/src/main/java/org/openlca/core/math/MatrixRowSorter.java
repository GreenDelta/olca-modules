package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Sorts a *quadratic* matrix (the technology matrix in openLCA) so that rows
 * with more entries are located at the bottom of the matrix.
 */
public class MatrixRowSorter {

	private IMatrixSolver solver;
	private IMatrix original;
	private TIntIntHashMap indexMap;

	public MatrixRowSorter(IMatrix original, IMatrixSolver solver) {
		this.original = original;
		this.solver = solver;
	}

	public IMatrix run() {
		List<Row> rows = collectRows();
		buildIndexMap(rows);
		IMatrix swapped = createSwappedMatrix();
		return swapped;
	}

	private List<Row> collectRows() {
		List<Row> rows = new ArrayList<>();
		for (int rowIdx = 0; rowIdx < original.rows(); rowIdx++) {
			Row row = new Row();
			row.idx = rowIdx;
			rows.add(row);
			for (int col = 0; col < original.columns(); col++) {
				double val = original.get(rowIdx, col);
				if (val != 0)
					row.entries++;
			}
		}
		return rows;
	}

	private void buildIndexMap(List<Row> rows) {
		Collections.sort(rows);
		indexMap = new TIntIntHashMap(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1, -1);
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			indexMap.put(row.idx, i);
		}
	}

	private IMatrix createSwappedMatrix() {
		IMatrix swapped = solver.matrix(original.rows(),
				original.rows());
		for (int r = 0; r < original.rows(); r++) {
			for (int c = 0; c < original.columns(); c++) {
				double val = original.get(r, c);
				if (val == 0)
					continue;
				int newRow = indexMap.get(r);
				int newCol = indexMap.get(c);
				swapped.set(newRow, newCol, val);
			}
		}
		return swapped;
	}

	private class Row implements Comparable<Row> {
		int idx = -1;
		int entries = 0;

		@Override
		public int compareTo(Row o) {
			int c = this.entries - o.entries;
			if (c != 0)
				return c;
			else
				return this.idx - o.idx;
		}
	}

}
