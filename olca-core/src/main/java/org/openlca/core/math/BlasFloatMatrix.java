package org.openlca.core.math;

import org.jblas.FloatMatrix;
import org.jblas.SimpleBlas;
import org.jblas.Solve;

public class BlasFloatMatrix implements IMatrix {

	private final FloatMatrix matrix;

	public BlasFloatMatrix(FloatMatrix matrix) {
		this.matrix = matrix;
	}

	public BlasFloatMatrix(int rowSize, int colSize) {
		this.matrix = new FloatMatrix(rowSize, colSize);
	}

	@Override
	public int getRowDimension() {
		return matrix.rows;
	}

	@Override
	public int getColumnDimension() {
		return matrix.columns;
	}

	@Override
	public void setEntry(int row, int col, double val) {
		matrix.put(row, col, (float) val);
	}

	@Override
	public double getEntry(int row, int col) {
		return matrix.get(row, col);
	}

	@Override
	public double[] getColumn(int i) {
		FloatMatrix col = matrix.getColumn(i);
		return convertToDoubles(col.data);
	}

	@Override
	public double[] getRow(int i) {
		FloatMatrix row = matrix.getRow(i);
		return convertToDoubles(row.data);
	}

	private double[] convertToDoubles(float[] floats) {
		if (floats == null)
			return new double[0];
		double[] doubles = new double[floats.length];
		for (int i = 0; i < floats.length; i++) {
			doubles[i] = floats[i];
		}
		return doubles;
	}

	@Override
	public IMatrix multiply(IMatrix with) {
		FloatMatrix withMatrix = unwrap(with);
		FloatMatrix result = matrix.mmul(withMatrix);
		return new BlasFloatMatrix(result);
	}

	@Override
	public IMatrix add(IMatrix toAdd) {
		FloatMatrix matrixToAdd = unwrap(toAdd);
		FloatMatrix result = matrix.add(matrixToAdd);
		return new BlasFloatMatrix(result);
	}

	@Override
	public IMatrix solve(IMatrix b) {
		FloatMatrix matrixB = unwrap(b);
		FloatMatrix matrixX = Solve.solve(matrix, matrixB);
		return new BlasFloatMatrix(matrixX);
	}

	@Override
	public IMatrix getInverse() {
		int size = matrix.rows;
		FloatMatrix inverse = Solve.solve(matrix, FloatMatrix.eye(size));
		return new BlasFloatMatrix(inverse);
	}

	@Override
	public IMatrix copy() {
		FloatMatrix copy = new FloatMatrix(matrix.rows, matrix.columns);
		SimpleBlas.copy(matrix, copy);
		return new BlasFloatMatrix(copy);
	}

	@Override
	public IMatrix subtract(IMatrix toSubtract) {
		FloatMatrix matrixToSubtract = unwrap(toSubtract);
		FloatMatrix result = matrix.sub(matrixToSubtract);
		return new BlasFloatMatrix(result);
	}

	private FloatMatrix unwrap(IMatrix matrix) {
		if (!(matrix instanceof BlasFloatMatrix))
			throw new IllegalArgumentException("incompatible matrix types");
		BlasFloatMatrix blasMatrix = (BlasFloatMatrix) matrix;
		return blasMatrix.matrix;
	}

}
