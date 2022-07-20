package org.openlca.core.matrix.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.ByteMatrixBuffer;
import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.UncertaintyType;

public abstract class MatrixExport {

	protected final IDatabase db;
	protected final File folder;
	protected final MatrixData data;

	protected MatrixExport(IDatabase db, File folder, MatrixData data) {
		this.db = db;
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

	public static MatrixExport toNpy(IDatabase db, File folder, MatrixData data) {
		return new NpyExport(db, folder, data);
	}

	public static void toNpy(File folder, MatrixReader matrix, String name) {
		if (folder == null || matrix == null)
			return;
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (IOException e) {
				throw new RuntimeException("failed to create folder " + folder, e);
			}
		}
		NpyMatrix.write(folder, name, matrix);
	}

	public static CsvExport toCsv(IDatabase db, File folder, MatrixData data) {
		return new CsvExport(db, folder, data);
	}

	/**
	 * Writes the indices and matrices into the respective export format.
	 */
	public void writeAll() {
		writeIndices();
		writeMatrices();
	}

	/**
	 * Writes only the matrices without the indices into the respective format.
	 */
	public void writeMatrices() {

		if (data.techMatrix != null) {
			write(data.techMatrix, "A");
		}
		if (data.enviMatrix != null) {
			write(data.enviMatrix, "B");
		}
		if (data.impactMatrix != null) {
			write(data.impactMatrix, "C");
		}
		if (data.costVector != null) {
			write(data.costVector, "costs");
		}

		// write the demand vector when available
		var techIndex = data.techIndex;
		var demand = data.demand;
		if (techIndex != null && demand != null) {
			var vec = new double[techIndex.size()];
			int refIdx = techIndex.of(demand.techFlow());
			if (refIdx >= 0) {
				vec[refIdx] = demand.value();
			}
			write(vec, "f");
		}

		uncertainties(data.techMatrix, data.techUncertainties, "A");
		uncertainties(data.enviMatrix, data.enviUncertainties, "B");
		uncertainties(data.impactMatrix, data.impactUncertainties, "C");
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

	protected abstract void write(MatrixReader matrix, String name);

	protected abstract void write(double[] vector, String name);

	protected abstract void write(ByteMatrixReader matrix, String name);

	/**
	 * Writes only the indices of the matrix data into the respective format.
	 */
	public abstract void writeIndices();

}
