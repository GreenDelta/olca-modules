package org.openlca.core.matrix.io.olcamat;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

class Matrices {

	static void writeDenseColumn(IMatrix m, File file) throws Exception {
		if (m == null || file == null)
			return;

		int length = 6 * 4 + m.rows() * m.columns() * 8;

		try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
				FileChannel channel = raf.getChannel()) {

			MappedByteBuffer buffer = channel.map(MapMode.READ_WRITE, 0, length);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			// format version -> 1
			buffer.putInt(1);
			// storage format -> 0 dense array in column major order
			buffer.putInt(0);
			// data type -> 0 64-bit floating point numbers
			buffer.putInt(0);
			// entry size -> 8 bytes
			buffer.putInt(8);

			// rows + columns
			buffer.putInt(m.rows());
			buffer.putInt(m.columns());

			// values
			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					buffer.putDouble(m.get(row, col));
				}
			}
		}
	}

	static IMatrix readDenseColumn(File file) throws Exception {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r");
				FileChannel channel = raf.getChannel()) {

			MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, raf.length());
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			// version
			buffer.getInt();
			// storage format
			buffer.getInt();
			// data type
			buffer.getInt();
			// entry size
			buffer.getInt();

			int rows = buffer.getInt();
			int cols = buffer.getInt();
			DenseMatrix m = new DenseMatrix(rows, cols);

			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					double val = buffer.getDouble();
					m.set(row, col, val);
				}
			}
			return m;
		}
	}
}
