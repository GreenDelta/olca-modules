package org.openlca.core.matrix.io;

import java.io.File;
import java.util.Objects;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.npy.Npy;
import org.openlca.npy.Npz;
import org.openlca.npy.arrays.Array2d;
import org.openlca.npy.arrays.NpyCharArray;
import org.openlca.npy.arrays.NpyDoubleArray;
import org.openlca.npy.arrays.NpyIntArray;

public class NpyMatrix {

	public static MatrixReader read(File file) {
		Objects.requireNonNull(file);
		boolean isNpz = file.getName().toLowerCase().endsWith(".npz");
		if (isNpz)
			return readNpz(file);
		var array = Npy.read(file).asDoubleArray();
		if (!Array2d.isValid(array))
			throw new IllegalArgumentException(
				"file " + file + " does not contain a matrix");
		if (array.hasFortranOrder()) {
			return new DenseMatrix(
				Array2d.rowCountOf(array),
				Array2d.columnCountOf(array),
				array.data());
		}
		// TODO transpose it
	}

	/**
	 * Write a matrix to a file in the given folder.
	 *
	 * @param folder the folder where the matrix should be stored.
	 * @param name   the name of the matrix file without file extension.
	 * @param matrix the matrix that should be written to the file.
	 */
	public static void write(File folder, String name, MatrixReader matrix) {
		if (folder == null || matrix == null)
			return;

		// write sparse matrices into the CSC format
		var m = matrix instanceof HashPointMatrix
			? CSCMatrix.of(matrix)
			: matrix;
		if (m instanceof CSCMatrix) {
			writeNpz(new File(folder, name + ".npz"), (CSCMatrix) m);
			return;
		}

		// write dense matrices in Fortran order
		var dense = matrix instanceof DenseMatrix
			? (DenseMatrix) matrix
			: DenseMatrix.of(matrix);
		var array = new NpyDoubleArray(
			new int[]{dense.rows, dense.columns}, dense.data, true);
		Npy.write(new File(folder, name + ".npy"), array);
	}

	private static void writeNpz(File file, CSCMatrix csc) {
		Npz.create(file, npz -> {
			Npz.write(npz, "format.npy",
				NpyCharArray.of("csc"));
			Npz.write(npz, "shape.npy",
				NpyIntArray.vectorOf(new int[]{csc.rows, csc.columns}));
			Npz.write(npz, "data.npy",
				NpyDoubleArray.vectorOf(csc.values));
			Npz.write(npz, "indptr.npy",
				NpyIntArray.vectorOf(csc.columnPointers));
			Npz.write(npz, "indices.npy",
				NpyIntArray.vectorOf(csc.rowIndices));
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

