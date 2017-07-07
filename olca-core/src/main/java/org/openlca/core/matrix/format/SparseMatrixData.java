package org.openlca.core.matrix.format;

import java.io.Serializable;

import org.openlca.core.matrix.format.HashMatrix.MatrixIterator;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * A class that holds the data of a sparse matrix. This class is only used for
 * mapping the data between native and Java functions.
 */
public class SparseMatrixData implements Serializable {

	private static final long serialVersionUID = 6102158570210807096L;

	public int numberOfEntries;
	public int rows;
	public int columns;
	public int[] rowIndices;
	public int[] columnIndices;
	public double[] values;

	public SparseMatrixData() {
	}

	/**
	 * Copies the data from the given matrix to a new instance of this class.
	 */
	public SparseMatrixData(HashMatrix matrix) {
		final TIntArrayList rowList = new TIntArrayList();
		final TIntArrayList colList = new TIntArrayList();
		final TDoubleArrayList valList = new TDoubleArrayList();
		matrix.iterate(new MatrixIterator() {
			@Override
			public void next(int row, int col, double val) {
				rowList.add(row);
				colList.add(col);
				valList.add(val);
			}
		});
		this.numberOfEntries = rowList.size();
		this.rows = matrix.rows();
		this.columns = matrix.columns();
		this.rowIndices = rowList.toArray();
		this.columnIndices = colList.toArray();
		this.values = valList.toArray();
	}

}
