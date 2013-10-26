package org.openlca.core.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

class JavaMatrix implements IMatrix {

	private final RealMatrix matrix;

	public JavaMatrix(RealMatrix matrix) {
		this.matrix = matrix;
	}

	public JavaMatrix(RealVector vector) {
		matrix = new Array2DRowRealMatrix(vector.getDimension(), 1);
		matrix.setColumnVector(0, vector);
	}

	public JavaMatrix(int rowSize, int colSize) {
		matrix = new Array2DRowRealMatrix(rowSize, colSize);
	}

	public RealMatrix getRealMatrix() {
		return matrix;
	}

	@Override
	public int getColumnDimension() {
		return matrix.getColumnDimension();
	}

	@Override
	public int getRowDimension() {
		return matrix.getRowDimension();
	}

	@Override
	public void setEntry(int row, int col, double val) {
		matrix.setEntry(row, col, val);
	}

	@Override
	public double getEntry(int row, int col) {
		return matrix.getEntry(row, col);
	}

	@Override
	public double[] getColumn(int i) {
		return matrix.getColumn(i);
	}

	@Override
	public double[] getRow(int i) {
		return matrix.getRow(i);
	}

	@Override
	public JavaMatrix multiply(IMatrix with) {
		RealMatrix withMatrix = unwrap(with);
		RealMatrix result = matrix.multiply(withMatrix);
		return new JavaMatrix(result);
	}

	@Override
	public JavaMatrix solve(IMatrix b) {
		RealMatrix matrixB = unwrap(b);
		RealMatrix matrixX = new LUDecomposition(matrix).getSolver().solve(
				matrixB);
		return new JavaMatrix(matrixX);
	}

	@Override
	public JavaMatrix getInverse() {
		RealMatrix inverse = new LUDecomposition(matrix).getSolver()
				.getInverse();
		return new JavaMatrix(inverse);
	}

	@Override
	public IMatrix copy() {
		return new JavaMatrix(matrix.copy());
	}

	@Override
	public JavaMatrix add(IMatrix toAdd) {
		RealMatrix matrixToAdd = unwrap(toAdd);
		RealMatrix result = matrix.add(matrixToAdd);
		return new JavaMatrix(result);
	}

	@Override
	public IMatrix subtract(IMatrix toSubtract) {
		RealMatrix matrixToSubtract = unwrap(toSubtract);
		RealMatrix result = matrix.subtract(matrixToSubtract);
		return new JavaMatrix(result);
	}

	private RealMatrix unwrap(IMatrix matrix) {
		if (!(matrix instanceof JavaMatrix))
			throw new IllegalArgumentException("incompatible matrix types");
		JavaMatrix javaMatrix = (JavaMatrix) matrix;
		return javaMatrix.matrix;
	}

}
