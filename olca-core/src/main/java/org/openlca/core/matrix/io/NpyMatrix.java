package org.openlca.core.matrix.io;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipFile;

import org.openlca.core.matrix.format.ByteMatrixReader;
import org.openlca.core.matrix.format.CSCByteMatrix;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.HashPointByteMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.npy.Array2d;
import org.openlca.npy.Npy;
import org.openlca.npy.NpyByteArray;
import org.openlca.npy.NpyCharArray;
import org.openlca.npy.NpyDoubleArray;
import org.openlca.npy.NpyIntArray;
import org.openlca.npy.Npz;
import org.openlca.util.Dirs;

public class NpyMatrix {

	public static MatrixReader read(File file) {
		Objects.requireNonNull(file);
		boolean isNpz = file.getName().toLowerCase().endsWith(".npz");
		return isNpz
			? readNpz(file)
			: readNpy(file);
	}

	/**
	 * Read the matrix stored in a npy or npz file with the given name from the
	 * given folder.
	 *
	 * @param folder the folder where the matrix is stored
	 * @param name   the name of matrix file without the npy or npz extension
	 * @return the matrix if it could be found in the folder or an empty option
	 * when there was no npy or npz file with the given name in the folder.
	 */
	public static Optional<MatrixReader> read(File folder, String name) {
		var npy = new File(folder, name + ".npy");
		if (npy.exists())
			return Optional.of(readNpy(npy));

		var npz = new File(folder, name + ".npz");
		return npz.exists()
			? Optional.of(readNpz(npz))
			: Optional.empty();
	}

	private static DenseMatrix readNpy(File file) {
		var array = Npy.read(file).asDoubleArray();
		if (!Array2d.isValid(array))
			throw new IllegalArgumentException(
				"file " + file + " does not contain a matrix");
		if (array.hasRowOrder()) {
			array = Array2d.switchOrder(array);
		}
		return new DenseMatrix(
			Array2d.rowCountOf(array),
			Array2d.columnCountOf(array),
			array.data());
	}

	private static MatrixReader readNpz(File file) {
		try (var npz = new ZipFile(file)) {
			assertCscFormat(npz);
			int[] shape = Npz.read(npz, "shape.npy")
				.asIntArray()
				.data();
			double[] values = Npz.read(npz, "data.npy")
				.asDoubleArray()
				.data();
			int[] columnPointers = Npz.read(npz, "indptr.npy")
				.asIntArray()
				.data();
			int[] rowIndices = Npz.read(npz, "indices.npy")
				.asIntArray()
				.data();
			return new CSCMatrix(
				shape[0], shape[1], values, columnPointers, rowIndices);
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Failed to read CSC file " + file, e);
		}
	}

	private static void assertCscFormat(ZipFile npz) {
		var entries = Npz.entries(npz);
		if (!entries.contains("format.npy"))
			throw new IllegalArgumentException(
				"Unsupported file format: " + npz +
				" is not a valid csc file");

		var format = Npz.read(npz, "format.npy");
		if (!format.isCharArray())
			throw new IllegalArgumentException(
				"Unsupported file format of " + npz +
				": format.npy does not contain a format string");
		var formatStr = format.asCharArray()
			.toString()
			.toLowerCase()
			.trim();
		if (!formatStr.equals("csc"))
			throw new IllegalArgumentException(
				"Unsupported file format of " + npz +
				": storage format " + formatStr + " is not supported");
	}

	/**
	 * Write a matrix to a file in the given folder.
	 *
	 * @param folder the folder where the matrix should be stored.
	 * @param name   the name of the matrix file without file extension.
	 * @param matrix the matrix that should be written to the file.
	 */
	public static File write(File folder, String name, MatrixReader matrix) {
		Dirs.createIfAbsent(folder);

		// write sparse matrices into the CSC format
		var m = matrix instanceof HashPointMatrix
			? CSCMatrix.of(matrix)
			: matrix;
		if (m instanceof CSCMatrix) {
			var file = new File(folder, name + ".npz");
			writeNpz(file, (CSCMatrix) m);
			return file;
		}

		// write dense matrices in Fortran order
		var dense = matrix instanceof DenseMatrix
			? (DenseMatrix) matrix
			: DenseMatrix.of(matrix);
		var file = new File(folder, name + ".npy");
		Npy.write(file,
			NpyDoubleArray.columnOrderOf(dense.data, dense.rows, dense.columns));
		return file;
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

	public static File write(File folder, String name, ByteMatrixReader matrix) {
		Dirs.createIfAbsent(folder);

		// write sparse matrices into the CSC format
		var m = matrix instanceof HashPointByteMatrix
			? ((HashPointByteMatrix) matrix).compress()
			: matrix;
		if (m instanceof CSCByteMatrix) {
			var file = new File(folder, name + ".npz");
			writeNpzBytes(file, (CSCByteMatrix) m);
			return file;
		}

		// write dense matrices in Fortran order
		var dense = matrix instanceof DenseByteMatrix
			? (DenseByteMatrix) matrix
			: DenseByteMatrix.of(matrix);
		var file = new File(folder, name + ".npy");
		Npy.write(file,
			NpyByteArray.columnOrderOf(dense.data, dense.rows, dense.columns));
		return file;
	}

	private static void writeNpzBytes(File file, CSCByteMatrix csc) {
		Npz.create(file, npz -> {
			Npz.write(npz, "format.npy",
				NpyCharArray.of("csc"));
			Npz.write(npz, "shape.npy",
				NpyIntArray.vectorOf(new int[]{csc.rows, csc.columns}));
			Npz.write(npz, "data.npy",
				NpyByteArray.vectorOf(csc.values));
			Npz.write(npz, "indptr.npy",
				NpyIntArray.vectorOf(csc.columnPointers));
			Npz.write(npz, "indices.npy",
				NpyIntArray.vectorOf(csc.rowIndices));
		});
	}

	public static Optional<ByteMatrixReader> readBytes(File folder, String name) {
		var npy = new File(folder, name + ".npy");
		if (npy.exists())
			return Optional.of(readNpyBytes(npy));
		var npz = new File(folder, name + ".npz");
		return npz.exists()
			? Optional.of(readNpzBytes(npz))
			: Optional.empty();
	}

	private static DenseByteMatrix readNpyBytes(File file) {
		var array = Npy.read(file).asByteArray();
		if (!Array2d.isValid(array))
			throw new IllegalArgumentException(
				"file " + file + " does not contain a matrix");
		if (array.hasRowOrder()) {
			array = Array2d.switchOrder(array);
		}
		return new DenseByteMatrix(
			Array2d.rowCountOf(array),
			Array2d.columnCountOf(array),
			array.data()
		);
	}

	private static ByteMatrixReader readNpzBytes(File file) {
		try (var npz = new ZipFile(file)) {
			int[] shape = Npz.read(npz, "shape.npy")
				.asIntArray()
				.data();
			byte[] values = Npz.read(npz, "data.npy")
				.asByteArray()
				.data();
			int[] columnPointers = Npz.read(npz, "indptr.npy")
				.asIntArray()
				.data();
			int[] rowIndices = Npz.read(npz, "indices.npy")
				.asIntArray()
				.data();
			return new CSCByteMatrix(
				shape[0], shape[1], values, columnPointers, rowIndices);
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Failed to read CSC file " + file, e);
		}
	}

}

