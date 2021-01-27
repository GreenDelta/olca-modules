package org.openlca.core.matrix.format;

public final class MatrixConverter {

	private MatrixConverter() {
	}

	public static DenseMatrix dense(MatrixReader m) {
		if (m instanceof DenseMatrix)
			return (DenseMatrix) m;

		int rows = m.rows();
		int cols = m.columns();
		var d = new DenseMatrix(rows, cols);

		if (m instanceof HashPointMatrix) {
			var s = (HashPointMatrix) m;
			s.iterate(d::set);
			return d;
		}

		// default implementation
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				d.set(row, col, m.get(row, col));
			}
		}
		return d;
	}

	public static HashPointMatrix hashSparse(Matrix matrix) {
		if (matrix instanceof HashPointMatrix)
			return (HashPointMatrix) matrix;

		int rows = matrix.rows();
		int cols = matrix.columns();
		HashPointMatrix sparse = new HashPointMatrix(rows, cols);

		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				double val = matrix.get(row, col);
				if (val == 0)
					continue;
				sparse.set(row, col, val);
			}
		}
		return sparse;
	}

}
