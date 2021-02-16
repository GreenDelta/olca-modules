package org.openlca.core.matrix.io;

import java.io.File;

import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixBuffer;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.CSCByteMatrix;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.format.HashPointByteMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.UncertaintyType;

class NpyExport extends MatrixExport {

	NpyExport(File folder, MatrixData data) {
		super(folder, data);
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
		if (matrix == null)
			return;
		var m = matrix instanceof HashPointMatrix
			? CSCMatrix.of(matrix)
			: matrix;
		if (m instanceof CSCMatrix) {
			var csc = (CSCMatrix) m;
			Npz.save(new File(folder, name + ".npz"), csc);
		} else {
			Npy.save(new File(folder, name + ".npy"), m);
		}
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

	private void write(MatrixReader host, UMatrix umatrix, String prefix) {
		if (host == null || umatrix == null)
			return;

		var types = new ByteMatrixBuffer();
		var builders = new MatrixBuilder[3];

		umatrix.each((row, col, cell) -> {

			var type = cell.type();
			if (type == null || type == UncertaintyType.NONE)
				return;
			var values = cell.values();
			if (values == null || values.length == 0)
				return;

			types.set(row, col, UncertaintyType.byteIndexOf(type));
			for (int i = 0; i < values.length; i++) {
				if (i > 2)
					break;
				var builder = builders[i];
				if (builder == null) {
					builder = new MatrixBuilder();
					builders[i] = builder;
				}
				builder.set(row, col, values[i]);
			}
		});

		if (types.isEmpty())
			return;

		// write the type matrix
		types.minSize(host.rows(), host.columns());
		var typeMatrix = types.finish();
		write(typeMatrix, prefix + "_utype");

		// write the value matrices
		for (int i = 0; i < builders.length; i++) {
			var builder = builders[i];
			if (builder == null || builder.isEmpty())
				continue;
			builder.minSize(host.rows(), host.columns());
			var matrix = builder.finish();
			write(matrix, prefix + "_u" + i);
		}
	}



}
