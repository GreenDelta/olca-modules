package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.Matrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

class DenseWriter {

	private final File file;
	private final Matrix matrix;

	DenseWriter(File file, Matrix matrix) {
		this.file = file;
		this.matrix = matrix;
	}

	void run() {
		try (var f = new RandomAccessFile(file, "rw");
			 var channel = f.getChannel()) {

			// write the header
			var head = new Header();
			head.dtype = "<f8";
			head.shape = new int[]{matrix.rows(), matrix.columns()};
			head.fortranOrder = true;
			channel.write(head.toByteBuffer());

			// write the data
			var buffer = ByteBuffer.allocate(matrix.rows() * 8);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			for (int col = 0; col < matrix.columns(); col++) {
				for (int row = 0; row < matrix.rows(); row++) {
					buffer.putDouble(matrix.get(row, col));
				}
				buffer.flip();
				channel.write(buffer);
				buffer.clear();
			}
		} catch (IOException e) {
			throw new RuntimeException("failed to write matrix to " + file, e);
		}
	}
}
