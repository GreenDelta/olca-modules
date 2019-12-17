package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Supports reading and writing dense matrices and vectors from NPY files. NPY
 * is a fast binary storage format for n-dimensional arrays used in NumPy. We
 * only support a subset of this format here.
 * <p>
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
		return DenseReader.read(file);
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

	/**
	 * Reads a vector of floating point numbers from the given stream which
	 * must be a NPY file where the position of the stream is the first byte in
	 * that file (the header is read within this method).
	 */
	static double[] readVector(InputStream stream) throws IOException {
		Header h = Header.read(stream);

		// check the length
		if (h.shape == null || h.shape.length == 0)
			return new double[0];
		int len = h.shape[0];
		if (len <= 0)
			return new double[0];

		// check the data type
		DType dtype = h.getDType();
		if (dtype != DType.Float64)
			throw new IllegalArgumentException(
					"not a supported floating point type " + dtype);

		// allocate resources
		int dsize = dtype.size();
		byte[] bytes = new byte[dsize];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(h.getByteOrder());
		double[] v = new double[len];

		// read the vector
		for (int i = 0; i < len; i++) {
			if (stream.read(bytes) != dsize)
				break;
			v[i] = buf.getDouble();
			buf.position(0);
		}
		return v;
	}

	public static void save(File file, double[] vector) {
		// TODO not yet implemented
	}

	/**
	 * Reads an integer vector from the given stream which must be a NPY file
	 * where the position of the stream is the first byte in that file (the
	 * header is read within this method).
	 */
	static int[] readIntVector(InputStream stream) throws IOException {
		Header h = Header.read(stream);

		// check the length
		if (h.shape == null || h.shape.length == 0)
			return new int[0];
		int len = h.shape[0];
		if (len <= 0)
			return new int[0];

		// check the data type
		DType dtype = h.getDType();
		if (dtype != DType.Int32 && dtype != DType.Int64)
			throw new IllegalArgumentException(
					"not a supported integer type " + dtype);

		// allocate resources
		int dsize = dtype.size();
		byte[] bytes = new byte[dsize];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(h.getByteOrder());
		int[] v = new int[len];

		// read the vector
		for (int i = 0; i < len; i++) {
			if (stream.read(bytes) != dsize)
				break;
			v[i] = buf.getInt();
			buf.position(0);
		}
		return v;
	}

	/**
	 * Writes the given vector including its header to the given output stream.
	 */
	static void write(OutputStream out, int[] v) throws IOException {
		Header h = new Header();
		h.shape = new int[]{v.length};
		h.dtype = "<i4";
		h.fortranOrder = false;
		h.write(out);
		ByteBuffer buff = ByteBuffer.allocate(v.length * 4);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.asIntBuffer().put(v);
		out.write(buff.array());
	}

	/**
	 * Writes the given vector including its header to the given output stream.
	 */
	static void write(OutputStream out, double[] v) throws IOException {
		Header h = new Header();
		h.shape = new int[]{v.length};
		h.dtype = "<f8";
		h.fortranOrder = false;
		h.write(out);
		ByteBuffer buff = ByteBuffer.allocate(v.length * 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.asDoubleBuffer().put(v);
		out.write(buff.array());
	}
}
