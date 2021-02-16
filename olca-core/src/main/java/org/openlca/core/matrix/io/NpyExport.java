package org.openlca.core.matrix.io;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.CSCByteMatrix;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.format.HashPointByteMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;

class NpyExport extends MatrixExport {

	NpyExport(IDatabase db, File folder, MatrixData data) {
		super(db, folder, data);
	}

	@Override
	public void writeIndices() {
		// TODO: do the things that are currently done in the
		// library export here
	}

	@Override
	protected void write(double[] vector, String name) {
		if (vector == null)
			return;
		var file = new File(folder, name + ".npy");
		Npy.save(file, vector);
	}

	@Override
	protected void write(MatrixReader matrix, String name) {
		toNpy(folder, matrix, name);
	}

	@Override
	protected void write(ByteMatrixReader matrix, String name) {
		var m = matrix instanceof HashPointByteMatrix
			? ((HashPointByteMatrix) matrix).compress()
			: matrix;
		if (m instanceof CSCByteMatrix) {
			Npz.save(new File(folder, name + ".npz"), (CSCByteMatrix) m);
		} else {
			var dense = m instanceof DenseByteMatrix
				? (DenseByteMatrix) m
				: new DenseByteMatrix(m);
			Npy.save(new File(folder, name + ".npy"), dense);
		}
	}
}
