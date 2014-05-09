package org.openlca.eigen;

import org.openlca.core.math.IMatrix;
import org.openlca.eigen.HashMatrix.MatrixIterator;

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
		final DenseMatrix m = new DenseMatrix(sparseMatrix.getRowDimension(),
				sparseMatrix.getColumnDimension());
		sparseMatrix.iterate(new MatrixIterator() {
			@Override
			public void next(int row, int col, double val) {
				m.setEntry(row, col, val);
			}
		});
		return m;
	}

	public static DenseMatrix asDenseMatrix(DenseFloatMatrix floatMatrix) {
		int rows = floatMatrix.getRowDimension();
		int cols = floatMatrix.getColumnDimension();
		DenseMatrix m = new DenseMatrix(rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				m.setEntry(row, cols, floatMatrix.getEntry(row, col));
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
		int rows = matrix.getRowDimension();
		int cols = matrix.getColumnDimension();
		DenseFloatMatrix m = new DenseFloatMatrix(rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				m.setEntry(row, cols, matrix.getEntry(row, col));
			}
		}
		return m;
	}

	public static DenseFloatMatrix asDenseFloatMatrix(HashMatrix sparseMatrix) {
		final DenseFloatMatrix m = new DenseFloatMatrix(
				sparseMatrix.getRowDimension(),
				sparseMatrix.getColumnDimension());
		sparseMatrix.iterate(new MatrixIterator() {
			@Override
			public void next(int row, int col, double val) {
				m.setEntry(row, col, val);
			}
		});
		return m;
	}

	public static HashMatrix asHashMatrix(IMatrix matrix) {
		if (matrix instanceof HashMatrix)
			return (HashMatrix) matrix;
		int rows = matrix.getRowDimension();
		int cols = matrix.getColumnDimension();
		HashMatrix sparse = new HashMatrix(rows, cols);
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				double val = matrix.getEntry(row, col);
				if (Numbers.isZero(val))
					continue;
				sparse.setEntry(row, col, val);
			}
		}
		return sparse;
	}

}
