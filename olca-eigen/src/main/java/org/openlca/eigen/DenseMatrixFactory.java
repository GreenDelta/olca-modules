package org.openlca.eigen;

import org.openlca.core.math.IMatrixFactory;

/** An implementation that returns *always* dense matrices in double precision. */
public class DenseMatrixFactory implements IMatrixFactory<DenseMatrix> {

	@Override
	public DenseMatrix create(int rows, int columns) {
		return new DenseMatrix(rows, columns);
	}

}
