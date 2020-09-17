package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class DenseReader {

	/**
	 * Reads a dense matrix from the given NPY file.
	 */
	static DenseMatrix read(File file) {
		try (RandomAccessFile f = new RandomAccessFile(file, "r");
			 FileChannel channel = f.getChannel()) {

			// read and check the header
			Header header = HeaderReader.read(channel);
			checkMatrix(file, header);

			// allocate the matrix
			int rows = header.shape[0];
			int cols = header.shape[1];
			DenseMatrix matrix = new DenseMatrix(rows, cols);

			// read the data
			// f.seek(header.dataOffset);
			if (header.fortranOrder) {
				readColumnOrder(matrix, header, channel);
			} else {
				readRowOrder(matrix, header, channel);
			}
			return matrix;
		} catch (IOException e) {
			throw new RuntimeException("failed to read from " + file, e);
		}
	}

	/**
	 * Reads a the given column from the given file that must contain a dense
	 * matrix.
	 */
	static double[] readColumn(File file, int column) {
		try (var f = new RandomAccessFile(file, "r");
			 var channel = f.getChannel()) {

			// read and check the header
			Header header = HeaderReader.read(channel);
			checkMatrix(file, header);

			int rows = header.shape[0];
			int cols = header.shape[1];
			if (column >= cols) {
				throw new IndexOutOfBoundsException(
						"Matrix in " + file + " has only " + cols + " columns.");
			}

			// use 64 bit numbers for offset calculations
			// otherwise we could run into negative seeks
			long rows64 = rows;
			long cols64 = cols;

			double[] data = new double[rows];

			if (header.fortranOrder) {

				// read the column in Fortran order
				long offset = header.dataOffset + (column * rows64 * 8L);
				f.seek(offset);
				ByteBuffer buffer = ByteBuffer.allocate(rows * 8);
				buffer.order(header.getByteOrder());
				channel.read(buffer);
				buffer.flip();
				for (int row = 0; row < rows; row++) {
					data[row] = buffer.getDouble();
				}

			} else {

				ByteBuffer buffer = ByteBuffer.allocate(8);
				buffer.order(header.getByteOrder());
				for (int row = 0; row < rows; row++) {
					long offset = header.dataOffset
							+ ((row * cols64 + column) * 8L);
					f.seek(offset);
					channel.read(buffer);
					buffer.flip();
					data[row] = buffer.getDouble();
					buffer.clear();
				}
			}
			return data;
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to read column " + column + " from " + file, e);
		}
	}

	/**
	 * Reads the matrix diagonal from the given file.
	 */
	static double[] readDiagonal(File file) {
		try (var f = new RandomAccessFile(file, "r");
			 var chan = f.getChannel()) {

			var header = HeaderReader.read(chan);
			checkMatrix(file, header);
			int rows = header.shape[0];
			int cols = header.shape[1];
			int n = Math.min(rows, cols);
			var diag = new double[n];
			if (n == 0)
				return diag;

			var buffer = ByteBuffer.allocate(8);
			buffer.order(header.getByteOrder());

			int pos = header.dataOffset;
			for (int i = 0; i < n; i++) {
				f.seek(pos);
				chan.read(buffer);
				buffer.flip();
				diag[i] = buffer.getDouble();
				buffer.clear();
				pos += header.fortranOrder
						? (rows + 1) * 8
						: (cols + 1) * 8;
			}

			return diag;
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to read diagonal from " + file, e);
		}
	}

	/**
	 * Check that the NPY header of the given file describes a 2d matrix with
	 * 64 bit floating point numbers.
	 */
	private static void checkMatrix(File file, Header header) {
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
	}

	/**
	 * Reads the matrix data in column major order.
	 */
	private static void readColumnOrder(
			DenseMatrix matrix, Header header, FileChannel channel)
			throws IOException {
		int rows = matrix.rows;
		int cols = matrix.columns;
		ByteBuffer buffer = ByteBuffer.allocate(rows * 8);
		buffer.order(header.getByteOrder());
		int i = 0;
		for (int col = 0; col < cols; col++) {
			channel.read(buffer);
			buffer.flip();
			for (int row = 0; row < rows; row++) {
				matrix.data[i] = buffer.getDouble();
				i++;
			}
			buffer.clear();
		}
	}

	/**
	 * Reads the matrix data in row-major order.
	 */
	private static void readRowOrder(
			DenseMatrix matrix, Header header, FileChannel channel)
			throws IOException {
		int rows = matrix.rows;
		int cols = matrix.columns;
		ByteBuffer buffer = ByteBuffer.allocate(cols * 8);
		buffer.order(header.getByteOrder());
		for (int row = 0; row < rows; row++) {
			channel.read(buffer);
			buffer.flip();
			for (int col = 0; col < cols; col++) {
				matrix.set(row, col, buffer.getDouble());
			}
			buffer.clear();
		}
	}
}
