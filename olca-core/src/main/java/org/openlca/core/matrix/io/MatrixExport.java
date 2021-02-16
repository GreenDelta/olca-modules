package org.openlca.core.matrix.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.math3.linear.RealMatrix;
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

public abstract class MatrixExport {

	protected final File folder;
	protected final MatrixData data;

	protected MatrixExport(File folder, MatrixData data) {
		this.data = data;
		this.folder = folder;
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Writes only the indices of the matrix data into the respective format.
	 */
	public abstract void writeIndices();

	/**
	 * Writes only the matrices without the indices into the respective format.
	 */
	public void writeMatrices() {
		if (data.techMatrix != null) {
			write(data.techMatrix, "A");
		}
		if (data.flowMatrix != null) {
			write(data.flowMatrix, "B");
		}
		if (data.impactMatrix != null) {
			write(data.impactMatrix, "C");
		}
		if (data.costVector != null) {
			write(data.costVector, "costs");
		}



	}

	private void uncertainties(MatrixReader host, UMatrix u, String prefix) {
		if (host == null || u == null)
			return;

		var types = new ByteMatrixBuffer();
		var builders = new MatrixBuilder[3];

		u.each((row, col, cell) -> {

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
		npyOf(folder, typeMatrix, prefix + "_utype");

		// write the value matrices
		for (int i = 0; i < builders.length; i++) {
			var builder = builders[i];
			if (builder == null || builder.isEmpty())
				continue;
			builder.minSize(host.rows(), host.columns());
			var matrix = builder.finish();
			npyOf(folder, matrix, prefix + "_u" + i);
		}
	}

	/**
	 * Writes the indices and matrices into the respective export format.
	 */
	public void writeAll() {
		writeIndices();
		writeMatrices();
	}

	protected abstract void write(MatrixReader matrix, String name);

	protected abstract void write(double[] vector, String name);

	protected abstract void write(ByteMatrixReader matrix, String name);

	/**
	 * Creates the NPY or NPZ files of the given data in the given folder.
	 */
	public static void npyOf(File folder, MatrixData data) {
		if (data == null || folder == null)
			return;
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		// write the base matrices
		npyOf(folder, data.techMatrix, "A");
		npyOf(folder, data.flowMatrix, "B");
		npyOf(folder, data.impactMatrix, "C");

		// write the uncertainty matrices
		npyOf(folder, data.techMatrix, data.techUncertainties, "A");
		npyOf(folder, data.flowMatrix, data.enviUncertainties, "B");
		npyOf(folder, data.impactMatrix, data.impactUncertainties, "C");

	}

	private static void npyOf(File folder, MatrixReader matrix, String name) {
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

	private static void npyOf(
		File folder, MatrixReader host, UMatrix umatrix, String prefix) {
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
		npyOf(folder, typeMatrix, prefix + "_utype");

		// write the value matrices
		for (int i = 0; i < builders.length; i++) {
			var builder = builders[i];
			if (builder == null || builder.isEmpty())
				continue;
			builder.minSize(host.rows(), host.columns());
			var matrix = builder.finish();
			npyOf(folder, matrix, prefix + "_u" + i);
		}
	}

	private static void npyOf(
		File folder, ByteMatrixReader matrix, String name) {
		var m = matrix instanceof HashPointByteMatrix
			? ((HashPointByteMatrix) matrix).compress()
			: matrix;
		if (m instanceof CSCByteMatrix) {
			Npz.save(
				new File(folder, name + ".npz"),
				(CSCByteMatrix) m);
		} else {
			var dense = m instanceof DenseByteMatrix
				? (DenseByteMatrix) m
				: new DenseByteMatrix(m);
			Npy.save(new File(folder, name + ".npy"), dense);
		}
	}

}
