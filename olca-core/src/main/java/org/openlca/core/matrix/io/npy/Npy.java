package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseByteMatrix;
import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.MatrixReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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
	 * column-major (Fortran) or row-major (C) order with 64 bit floating point
	 * numbers are supported.
	 */
	public static DenseMatrix load(File file) {
		return DenseReader.read(file);
	}

	/**
	 * Loads a dense matrix of signed bytes from the given file. Only 2d
	 * matrices in column-major (Fortran) or row-major (C) order of 8 bit signed
	 * integers are supported.
	 */
	public static DenseByteMatrix loadByteMatrix(File file) {
		return DenseByteReader.read(file);
	}

	/**
	 * Loads a single column from a NPY file that contains a dense
	 * matrix.
	 */
	public static double[] loadColumn(File file, int column) {
		return DenseReader.readColumn(file, column);
	}

	/**
	 * Reads the diagonal from the dense matrix that is stored in
	 * the given file.
	 */
	public static double[] loadDiagonal(File file) {
		return DenseReader.readDiagonal(file);
	}

	/**
	 * Saves the given matrix as dense matrix in column-major order to the
	 * given file.
	 */
	public static void save(File file, MatrixReader matrix) {
		if (file == null || matrix == null)
			return;
		new DenseWriter(file, matrix).run();
	}

	public static void save(File file, DenseByteMatrix m) {
		if (m == null || file == null)
			return;
		try (var f = new RandomAccessFile(file, "rw");
			 var chan = f.getChannel()) {

			// write the header
			var header = new Header();
			header.dtype = "|i1";
			header.shape = new int[]{m.rows, m.columns};
			header.fortranOrder = true;
			chan.write(header.toByteBuffer());

			// write the data
			var buffer = ByteBuffer.wrap(m.data);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			chan.write(buffer);

		} catch (IOException e) {
			throw new RuntimeException("failed to write matrix to " + file, e);
		}
	}

	public static void save(File file, double[] vector) {
		if (vector == null || file == null)
			return;
		try (var stream = new FileOutputStream(file)) {
			write(stream, vector);
		} catch (IOException e) {
			throw new RuntimeException("failed to write vector to " + file, e);
		}
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

	static byte[] readByteVector(InputStream stream) throws IOException {
		var header = Header.read(stream);
		if (header.shape == null || header.shape.length == 0)
			return new byte[0];
		int len = header.shape[0];
		if (len <= 0)
			return new byte[0];
		var dtype = header.getDType();
		if (dtype != DType.Int8)
			throw new IllegalArgumentException(
				"not a supported byte type: " + dtype);
		var data = new byte[len];
		stream.read(data);
		return data;
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
		var h = new Header();
		h.shape = new int[]{v.length};
		h.dtype = "<f8";
		h.fortranOrder = false;
		h.write(out);
		var buff = ByteBuffer.allocate(v.length * 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.asDoubleBuffer().put(v);
		out.write(buff.array());
	}

	static void write(OutputStream out, byte[] v) throws IOException {
		var header = new Header();
		header.shape = new int[]{v.length};
		header.dtype = "|i1";
		header.fortranOrder = false;
		header.write(out);
		out.write(v);
	}
}
