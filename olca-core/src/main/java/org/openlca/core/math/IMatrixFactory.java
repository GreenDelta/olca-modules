package org.openlca.core.math;

public interface IMatrixFactory {

	IMatrix create(int rows, int columns);

	IMatrix create(double[][] values);

}
