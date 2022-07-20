package org.openlca.core.matrix.io;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.index.IxContext;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxFormat;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.npy.Npy;
import org.openlca.npy.NpyDoubleArray;

class NpyExport extends MatrixExport {

	NpyExport(IDatabase db, File folder, MatrixData data) {
		super(db, folder, data);
	}

	@Override
	public void writeIndices() {
		var context = IxContext.of(db);
		if (data.techIndex != null) {
			IxTechIndex.of(data.techIndex, context)
				.writeToDir(folder, IxFormat.PROTO);
		}
		if (data.enviIndex != null) {
			IxEnviIndex.of(data.enviIndex, context)
				.writeToDir(folder, IxFormat.PROTO);
		}
		if (data.impactIndex != null) {
			IxImpactIndex.of(data.impactIndex)
				.writeToDir(folder, IxFormat.PROTO);
		}
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
		NpyMatrix.write(folder, name, matrix);
	}

	@Override
	protected void write(ByteMatrixReader matrix, String name) {
		NpyMatrix.write(folder, name, matrix);
	}
}
