package org.openlca.core.math;

public class JavaMatrixFactory implements IMatrixFactory {

	@Override
	public IMatrix create(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public IMatrix createSparse(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public IMatrix createMatrix(int rows, int columns, double loadFactor) {
		return new JavaMatrix(rows, columns);
	}

	@Override
	public ISolver getDefaultSolver() {
		return new JavaSolver();
	}
}
