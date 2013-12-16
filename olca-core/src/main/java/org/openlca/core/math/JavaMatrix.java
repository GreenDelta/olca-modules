package org.openlca.core.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
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
	public IMatrix copy() {
		return new JavaMatrix(matrix.copy());
	}

}
