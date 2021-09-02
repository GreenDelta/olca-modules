package org.openlca.core.matrix.io;

import java.io.File;
import java.util.Objects;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.npy.Npz;
import org.openlca.npy.arrays.NpyCharArray;
import org.openlca.npy.arrays.NpyIntArray;

public class NpyMatrix {

	public static MatrixReader read(File file) {
		Objects.requireNonNull(file);
		boolean isNpz = file.getName().toLowerCase().endsWith(".npz");
		return isNpz
			? readNpz(file)
			: null; // TODO: read dense array
	}

	public static void write(File file, MatrixReader matrix) {
		if (file == null || matrix == null)
			return;
		var m = matrix instanceof HashPointMatrix
			? CSCMatrix.of(matrix)
			: matrix;


	}

	private void writeNpz(File file, CSCMatrix csc) {
		Npz.create(file, zipOut -> {
			var format = NpyCharArray.of("csc");
			Npz.write(zipOut, "format.npy", format);
			NpyIntArray.
		});
	}


	private static MatrixReader readNpz(File file) {

		var entries = Npz.entries(file);
		if (!entries.contains("format.npy"))
			throw new IllegalArgumentException(
				"Unsupported file format: " + file +
				" is not a valid csc file");

		var format = Npz.read(file, "format.npy");
		if (!format.isCharArray())
			throw new IllegalArgumentException(
				"Unsupported file format of " + file +
				": format.npy does not contain a format string");
		var storageFormat = format.asCharArray()
			.toString()
			.toLowerCase()
			.trim();

		if (!storageFormat.equals("csc"))
			throw new IllegalArgumentException(
				"Unsupported file format of " + file +
				": storage format " + storageFormat + " is not supported");

		try {
			int[] shape = Npz.read(file, "shape.npy")
				.asIntArray()
				.data();
			double[] values = Npz.read(file, "data.npy")
				.asDoubleArray()
				.data();
			int[] columnPointers = Npz.read(file, "indptr.npy")
				.asIntArray()
				.data();
			int[] rowIndices = Npz.read(file, "indices.npy")
				.asIntArray()
				.data();
			return new CSCMatrix(
				shape[0], shape[1], values, columnPointers, rowIndices);
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Failed to read CSC file " + file + ":" + e.getMessage());
		}
	}
}

