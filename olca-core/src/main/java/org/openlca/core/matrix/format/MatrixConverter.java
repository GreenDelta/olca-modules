package org.openlca.core.matrix.format;

import org.openlca.core.matrix.format.HashMatrix.MatrixIterator;

public final class MatrixConverter {

	private MatrixConverter() {
	}

	public static DenseMatrix asDenseMatrix(IMatrix matrix) {
		if (matrix instanceof DenseMatrix)
			return (DenseMatrix) matrix;
		if (matrix instanceof DenseFloatMatrix)
			return asDenseMatrix((DenseFloatMatrix) matrix);
		if (matrix instanceof HashMatrix)
			return asDenseMatrix((HashMatrix) matrix);
		else {
			throw new IllegalArgumentException("Unsupported matrix "
					+ "type for this package: " + matrix.getClass());
		}
	}

	public static DenseMatrix asDenseMatrix(HashMatrix sparseMatrix) {
		final DenseMatrix m = new DenseMatrix(sparseMatrix.rows(),
				sparseMatrix.columns());
		sparseMatrix.iterate(new MatrixIterator() {
			@Override
			public void next(int row, int col, double val) {
				m.set(row, col, val);
			}
		});
		return m;
	}

	public static DenseMatrix asDenseMatrix(DenseFloatMatrix floatMatrix) {
		int rows = floatMatrix.rows();
		int cols = floatMatrix.columns();
		DenseMatrix m = new DenseMatrix(rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				m.set(row, cols, floatMatrix.get(row, col));
			}
		}
		return m;
	}

	public static DenseFloatMatrix asDenseFloatMatrix(IMatrix matrix) {
		if (matrix instanceof DenseFloatMatrix)
			return (DenseFloatMatrix) matrix;
		if (matrix instanceof DenseMatrix)
			return asDenseFloatMatrix((DenseMatrix) matrix);
		if (matrix instanceof HashMatrix)
			return asDenseFloatMatrix((HashMatrix) matrix);
		else {
			throw new IllegalArgumentException("Unsupported matrix "
					+ "type for this package: " + matrix.getClass());
		}
	}

	public static DenseFloatMatrix asDenseFloatMatrix(DenseMatrix matrix) {
		int rows = matrix.rows();
		int cols = matrix.columns();
		DenseFloatMatrix m = new DenseFloatMatrix(rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				m.set(row, cols, matrix.get(row, col));
			}
		}
		return m;
	}

	public static DenseFloatMatrix asDenseFloatMatrix(HashMatrix sparseMatrix) {
		final DenseFloatMatrix m = new DenseFloatMatrix(
				sparseMatrix.rows(),
				sparseMatrix.columns());
		sparseMatrix.iterate(new MatrixIterator() {
			@Override
			public void next(int row, int col, double val) {
				m.set(row, col, val);
			}
		});
		return m;
	}

	public static HashMatrix asHashMatrix(IMatrix matrix) {
		if (matrix instanceof HashMatrix)
			return (HashMatrix) matrix;
		int rows = matrix.rows();
		int cols = matrix.columns();
		HashMatrix sparse = new HashMatrix(rows, cols);
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				double val = matrix.get(row, col);
				if (Numbers.isZero(val))
					continue;
				sparse.set(row, col, val);
			}
		}
		return sparse;
	}

}
