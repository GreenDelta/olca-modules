package org.openlca.core.math;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sorts a *quadratic* matrix (the technology matrix in openLCA) so that rows
 * with more entries are located at the bottom of the matrix.
 */
public class MatrixRowSorter {

	private IMatrixFactory<?> factory;
	private IMatrix original;
	private TIntIntHashMap indexMap;

	public MatrixRowSorter(IMatrix original, IMatrixFactory<?> factory) {
		this.original = original;
		this.factory = factory;
	}

	public IMatrix run() {
		List<Row> rows = collectRows();
		buildIndexMap(rows);
		IMatrix swapped = createSwappedMatrix();
		return swapped;
	}

	private List<Row> collectRows() {
		List<Row> rows = new ArrayList<>();
		for (int rowIdx = 0; rowIdx < original.getRowDimension(); rowIdx++) {
			Row row = new Row();
			row.idx = rowIdx;
			rows.add(row);
			for (int col = 0; col < original.getColumnDimension(); col++) {
				double val = original.getEntry(rowIdx, col);
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
		IMatrix swapped = factory.create(original.getRowDimension(),
				original.getRowDimension());
		for (int r = 0; r < original.getRowDimension(); r++) {
			for (int c = 0; c < original.getColumnDimension(); c++) {
				double val = original.getEntry(r, c);
				if (val == 0)
					continue;
				int newRow = indexMap.get(r);
				int newCol = indexMap.get(c);
				swapped.setEntry(newRow, newCol, val);
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
