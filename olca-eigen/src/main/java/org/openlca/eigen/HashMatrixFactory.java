package org.openlca.eigen;

import org.openlca.core.math.IMatrixFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashMatrixFactory implements IMatrixFactory<HashMatrix> {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public HashMatrix create(int rows, int columns) {
		log.trace("create a {}x{} hash map matrix in double precision", rows,
				columns);
		return new HashMatrix(rows, columns);
	}

}
