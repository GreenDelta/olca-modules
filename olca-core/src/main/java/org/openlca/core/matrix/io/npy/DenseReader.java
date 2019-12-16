package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

class DenseReader {

	private final File file;
	private final Header header;

	DenseReader(File file, Header header) {
		this.file = file;
		this.header = header;
	}

	DenseMatrix run() {
		int rows = header.shape[0];
		int cols = header.shape[1];
		DenseMatrix matrix = new DenseMatrix(rows, cols);
		try (RandomAccessFile f = new RandomAccessFile(file, "r");
			 FileChannel channel = f.getChannel()) {
			f.seek(header.dataOffset);

			if (header.fortranOrder) {

				// read data in column-major order
				ByteBuffer buffer = ByteBuffer.allocate(rows * 8);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
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

			} else {

				// read the data in row major order
				ByteBuffer buffer = ByteBuffer.allocate(cols * 8);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				for (int row = 0; row < rows; row++) {
					channel.read(buffer);
					buffer.flip();
					for (int col = 0; col < cols; col++) {
						matrix.set(row, col, buffer.getDouble());
					}
					buffer.clear();
				}
			}

			return matrix;
		} catch (IOException e) {
			throw new RuntimeException("failed to read from " + file, e);
		}
	}
}
