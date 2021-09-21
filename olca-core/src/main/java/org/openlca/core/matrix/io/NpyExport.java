package org.openlca.core.matrix.io;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.npy.Npy;
import org.openlca.npy.NpyDoubleArray;

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
		var array = new NpyDoubleArray(new int[]{vector.length}, vector, false);
		Npy.write(file, array);
	}

	@Override
	protected void write(MatrixReader matrix, String name) {
		toNpy(folder, matrix, name);
	}

	@Override
	protected void write(ByteMatrixReader matrix, String name) {
		NpyMatrix.write(folder, name, matrix);
	}
}
