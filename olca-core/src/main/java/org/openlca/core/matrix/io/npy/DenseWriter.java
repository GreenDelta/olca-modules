package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class DenseWriter {

	private final File file;
	private final IMatrix matrix;

	DenseWriter(File file, IMatrix matrix) {
		this.file = file;
		this.matrix = matrix;
	}

	void run() {
		try (RandomAccessFile f = new RandomAccessFile(file, "rw");
			 FileChannel channel = f.getChannel()) {

			Header head = new Header();
			head.dtype = "<f8";
			head.shape = new int[]{matrix.rows(), matrix.columns()};
			head.fortranOrder = true;
			byte[] headerBytes = (head.toString()).getBytes("ascii");

			// see https://docs.scipy.org/doc/numpy-1.14.2/neps/npy-format.html
			// 6 bytes: “x93NUMPY”
			// 1 byte: major version
			// 1 byte: minor version
			// 2 bytes: header length (as unsigned short)
			// h bytes: header
			// 1 byte: '\n'
			// + padding so that it is divisible by 16
			int unpadded = 10 + headerBytes.length + 1;
			int padding = 0;
			if ((unpadded % 16) != 0) {
				padding = 16 - (unpadded % 16);
			}

			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE, 0,
					unpadded + padding + (matrix.rows() * matrix.columns() * 8));
			buf.order(ByteOrder.LITTLE_ENDIAN);

			// magic
			buf.put((byte) 0x93);
			buf.put("NUMPY".getBytes());
			// major version
			buf.put((byte) 0x01);
			// minor version
			buf.put((byte) 0x00);
			// header length
			buf.putShort((short) ((headerBytes.length + 1 + padding) & 0xffff));

			// header & padding
			buf.put(headerBytes);
			for (int i = 0; i < padding; i++) {
				buf.put((byte)' ');
			}
			buf.put((byte)'\n');

			// write the matrix data
			if (matrix instanceof DenseMatrix) {
				DenseMatrix dmatrix = (DenseMatrix)matrix;
				buf.asDoubleBuffer().put(dmatrix.getData());
			} else {
				for (int col = 0; col < matrix.columns(); col++) {
					for (int row = 0; row < matrix.rows(); row++) {
						buf.putDouble(matrix.get(row, col));
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to write matrix to " + file, e);
		}
	}

}
