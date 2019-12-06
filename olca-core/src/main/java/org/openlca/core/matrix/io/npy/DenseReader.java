package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
		try (RandomAccessFile f = new RandomAccessFile(file, "rw");
			 FileChannel channel = f.getChannel()) {
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_ONLY, header.dataOffset,
					(rows * cols * 8));
			buf.order(ByteOrder.LITTLE_ENDIAN);
			if (header.fortranOrder) {
				// if we are in fortran order, we can directly
				// map the data into memory
				buf.asDoubleBuffer().get(matrix.getData());
			} else {
				DoubleBuffer dbuf = buf.asDoubleBuffer();
				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {
						matrix.set(row, col, dbuf.get());
					}
				}
			}
			return matrix;
		} catch (IOException e) {
			throw new RuntimeException("failed to read from " + file, e);
		}
	}
}
