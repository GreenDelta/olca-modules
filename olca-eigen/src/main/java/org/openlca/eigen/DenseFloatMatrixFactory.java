package org.openlca.eigen;

import org.openlca.core.math.IMatrixFactory;

/** An implementation that returns *always* dense matrices in single precision. */
public class DenseFloatMatrixFactory implements
		IMatrixFactory<DenseFloatMatrix> {

	@Override
	public DenseFloatMatrix create(int rows, int columns) {
		return new DenseFloatMatrix(rows, columns);
	}

}
