package org.openlca.core.math;

public class JavaMatrixFactory implements IMatrixFactory<JavaMatrix> {

	@Override
	public JavaMatrix create(int rows, int columns) {
		return new JavaMatrix(rows, columns);
	}
}
