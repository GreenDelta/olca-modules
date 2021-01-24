package org.openlca.core.matrix.io.npy;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import org.openlca.core.matrix.format.DenseByteMatrix;

class DenseByteReader {

	static DenseByteMatrix read(File file) {
		try (var f = new RandomAccessFile(file, "r");
			 var chan = f.getChannel()) {

			// read and check the matrix header
			var header = HeaderReader.read(chan);
			check(file, header);
			int rows = header.shape[0];
			int cols = header.shape[1];

			// read data in Fortran order
			if (header.fortranOrder) {
				var buffer = ByteBuffer.allocate(rows * cols);
				chan.read(buffer);
				return new DenseByteMatrix(rows, cols, buffer.array());
			}

			// read data in C order
			var matrix = new DenseByteMatrix(rows, cols);
			var buffer = ByteBuffer.allocate(cols);
			for (int row = 0; row < rows; row++) {
				chan.read(buffer);
				buffer.flip();
				for (int col = 0; col < cols; col++) {
					matrix.set(row, col, buffer.get());
				}
				buffer.clear();
			}
			return matrix;

		} catch (IOException e) {
			throw new RuntimeException("failed to read from " + file, e);
		}
	}

	private static void check(File file, Header header) {
		var shape = header.shape;
		if (shape.length != 2
			|| shape[0] < 1
			|| shape[1] < 1) {
			throw new IllegalArgumentException(
				"invalid header shape: " + header + " in " + file);
		}
		if (header.getDType() != DType.Int8) {
			throw new IllegalArgumentException(
				"invalid data type: " + header.dtype + " in " + file);
		}
	}
}
