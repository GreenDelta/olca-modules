package org.openlca.util;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;

public class MatrixUtils {

	public static IMatrix create(double[][] data, IMatrixFactory<?> factory) {
		int rows = data.length;
		int cols = 1;
		for (int row = 0; row < rows; row++) {
			cols = Math.max(cols, data[row].length);
		}
		IMatrix m = factory.create(rows, cols);
		for (int row = 0; row < rows; row++) {
			double[] rowVals = data[row];
			for (int col = 0; col < rowVals.length; col++)
				m.set(row, col, rowVals[col]);
		}
		return m;
	}

}
