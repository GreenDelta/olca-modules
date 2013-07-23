package org.openlca.core.math;

import org.jblas.DoubleMatrix;
import org.jblas.SimpleBlas;
import org.jblas.Solve;

public class BlasMatrix implements IMatrix {

	private final DoubleMatrix matrix;

	public BlasMatrix(DoubleMatrix matrix) {
		this.matrix = matrix;
	}

	public BlasMatrix(int rowSize, int colSize) {
		this.matrix = new DoubleMatrix(rowSize, colSize);
	}

	@Override
	public double[] getColumn(int i) {
		DoubleMatrix col = matrix.getColumn(i);
		return col.data;
	}

	@Override
	public int getColumnDimension() {
		return matrix.columns;
	}

	@Override
	public double getEntry(int row, int col) {
		return matrix.get(row, col);
	}

	@Override
	public IMatrix getInverse() {
		int size = matrix.rows;
		DoubleMatrix inverse = Solve.solve(matrix, DoubleMatrix.eye(size));
		return new BlasMatrix(inverse);
	}

	@Override
	public double[] getRow(int i) {
		DoubleMatrix row = matrix.getRow(i);
		return row.data;
	}

	@Override
	public int getRowDimension() {
		return matrix.rows;
	}

	@Override
	public IMatrix multiply(IMatrix with) {
		DoubleMatrix withMatrix = unwrap(with);
		DoubleMatrix result = matrix.mmul(withMatrix);
		return new BlasMatrix(result);
	}

	@Override
	public void setEntry(int row, int col, double val) {
		matrix.put(row, col, val);
	}

	@Override
	public IMatrix solve(IMatrix b) {
		DoubleMatrix matrixB = unwrap(b);
		DoubleMatrix matrixX = Solve.solve(matrix, matrixB);
		return new BlasMatrix(matrixX);
	}

	private DoubleMatrix unwrap(IMatrix matrix) {
		if (!(matrix instanceof BlasMatrix))
			throw new IllegalArgumentException("incompatible matrix types");
		BlasMatrix blasMatrix = (BlasMatrix) matrix;
		return blasMatrix.matrix;
	}

	@Override
	public IMatrix copy() {
		DoubleMatrix copy = new DoubleMatrix(matrix.rows, matrix.columns);
		SimpleBlas.copy(matrix, copy);
		return new BlasMatrix(copy);
	}

	@Override
	public IMatrix add(IMatrix toAdd) {
		DoubleMatrix matrixToAdd = unwrap(toAdd);
		DoubleMatrix result = matrix.add(matrixToAdd);
		return new BlasMatrix(result);
	}

	@Override
	public IMatrix subtract(IMatrix toSubtract) {
		DoubleMatrix matrixToSubtract = unwrap(toSubtract);
		DoubleMatrix result = matrix.sub(matrixToSubtract);
		return new BlasMatrix(result);
	}

	public DoubleMatrix getNativeMatrix() {
		return matrix;
	}

}
