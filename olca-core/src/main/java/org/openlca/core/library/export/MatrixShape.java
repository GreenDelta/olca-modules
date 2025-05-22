package org.openlca.core.library.export;

import org.openlca.core.matrix.format.CompressedRowMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.JavaMatrix;
import org.openlca.core.matrix.format.MatrixReader;

class MatrixShape {

	static MatrixReader ensureIfPresent(MatrixReader r, int rows, int cols) {
		if (r == null)
			return null;

		if (r.rows() >= rows && r.columns() >= cols)
			return r;
		int rs = Math.max(r.rows(), rows);
		int cs = Math.max(r.columns(), cols);

		return switch (r.asMutable()){
			case DenseMatrix dense -> extendDense(dense, rs, cs);
			case CompressedRowMatrix crs -> extendCrs(crs, rs, cs);
			case HashPointMatrix hpm -> extendHashPoints(hpm, rs, cs);
			case JavaMatrix jm -> extendJavaMatrix(jm, rs, cs);
		};
	}

	private static MatrixReader extendDense(
			DenseMatrix dense, int rows, int cols
	) {

		var next = new DenseMatrix(rows, cols);

		// as the data is stored in column-major order, we can directly
		// copy the columns into the new matrix when the number of rows
		// is the same
		if (dense.rows == rows) {
			System.arraycopy(dense.data, 0, next.data, 0, dense.data.length);
			return next;
		}

		// copy the columns into the new matrix
		for (int col = 0; col < cols; col++) {
			if (col > (dense.columns - 1))
				break;
			System.arraycopy(
					dense.data, col * dense.rows, next.data,  col * rows, dense.rows);
		}

		return next;
	}

	private static MatrixReader extendCrs(
			CompressedRowMatrix crs, int rows, int cols
	) {
		var hash = new HashPointMatrix(rows, cols);
		crs.iterate(hash::set);
		return hash;
	}

	private static MatrixReader extendHashPoints(
			HashPointMatrix hpm, int rows, int cols
	) {
		hpm.rows = rows;
		hpm.cols = cols;
		return hpm;
	}

	private static MatrixReader extendJavaMatrix(
			JavaMatrix jm, int rows, int cols
	) {
		var m = jm.getRealMatrix();
		var next = m.createMatrix(rows, cols);
		next.setSubMatrix(m.getData(), 0, 0);
		return new JavaMatrix(next);
	}
}
