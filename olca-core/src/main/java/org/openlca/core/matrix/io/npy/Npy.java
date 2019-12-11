package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

import java.io.File;
import java.io.InputStream;

/**
 * Supports reading and writing dense matrices and vectors from NPY files. NPY
 * is a fast binary storage format for n-dimensional arrays used in NumPy. We
 * only support a subset of this format here.
 *
 * See https://numpy.org/devdocs/reference/generated/numpy.lib.format.html
 */
public final class Npy {

	private Npy() {
	}

	/**
	 * Loads a dense matrix from the given file. Only 2d matrices in
	 * column-major (Fortran order) or row-major (C order) order with 64 bit
	 * floating point numbers are supported.
	 */
	public static DenseMatrix load(File file) {
		Header header = Header.read(file);
		int[] shape = header.shape;
		if (shape == null
				|| shape.length != 2
				|| shape[0] < 1
				|| shape[1] < 1) {
			throw new IllegalArgumentException(
					"invalid header shape " + header + ": " + file);
		}
		if (header.getDType() != DType.Float64) {
			throw new IllegalArgumentException(
					"unsupported data type: " + header.dtype + ": " + file);
		}
		return new DenseReader(file, header).run();
	}

	/**
	 * Saves the given matrix as dense matrix in column-major order to the
	 * given file.
	 */
	public static void save(File file, IMatrix matrix) {
		if (file == null || matrix == null)
			return;
		new DenseWriter(file, matrix).run();
	}

	public static double[] loadVector(File file) {
		// TODO not yet implemented
		return null;
	}

	double[] readFloatVector(InputStream stream) {
		//
		return null;
	}

	public static void save(File file, double[] vector) {
		// TODO not yet implemented
	}

	static int[] readIntVector(InputStream stream) {
		// TODO not yet implemented
		return null;
	}
}
