package org.openlca.core.math;

public class JavaMatrixFactory implements IMatrixFactory {

	@Override
	public IMatrix create(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public IMatrix create(double[][] values) {
		int rows = values.length;
		int cols = 1;
		for (int row = 0; row < rows; row++) {
			cols = Math.max(cols, values[row].length);
		}
		IMatrix m = create(rows, cols);
		for (int row = 0; row < rows; row++) {
			double[] rowVals = values[row];
			for (int col = 0; col < rowVals.length; col++)
				m.setEntry(row, col, rowVals[col]);
		}
		return m;
	}

}
