package org.openlca.core.matrix.io.olcamat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

class Matrices {

	static void writeDenseColumn(IMatrix m, File file) throws Exception {
		try (FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream buffer = new BufferedOutputStream(fos)) {

			// byte buffers for int and double
			ByteBuffer i32 = ByteBuffer.allocate(4);
			ByteBuffer f64 = ByteBuffer.allocate(8);
			i32.order(ByteOrder.LITTLE_ENDIAN);
			f64.order(ByteOrder.LITTLE_ENDIAN);

			// format version -> 1
			i32.putInt(1);
			buffer.write(i32.array());
			i32.clear();

			// storage format -> 0 dense array in column major order
			i32.putInt(0);
			buffer.write(i32.array());
			i32.clear();

			// data type -> 0 64-bit floating point numbers
			i32.putInt(0);
			buffer.write(i32.array());
			i32.clear();

			// entry size -> 8 bytes
			i32.putInt(8);
			buffer.write(i32.array());
			i32.clear();

			// rows + columns
			i32.putInt(m.rows());
			buffer.write(i32.array());
			i32.clear();
			i32.putInt(m.columns());
			buffer.write(i32.array());

			// values
			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					f64.putDouble(m.get(row, col));
					buffer.write(f64.array());
					f64.clear();
				}
			}
		}
	}

	static IMatrix readDenseColumn(File file) throws Exception {

		ByteBuffer intBuffer = ByteBuffer.allocate(4);
		intBuffer.order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer doubleBuffer = ByteBuffer.allocate(8);
		doubleBuffer.order(ByteOrder.LITTLE_ENDIAN);

		try (FileInputStream fis = new FileInputStream(file);
				FileChannel channel = fis.getChannel()) {

			// version
			readInt(channel, intBuffer);
			// storage format
			readInt(channel, intBuffer);
			// data type
			readInt(channel, intBuffer);
			// entry size
			readInt(channel, intBuffer);

			int rows = readInt(channel, intBuffer);
			int cols = readInt(channel, intBuffer);
			DenseMatrix m = new DenseMatrix(rows, cols);

			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					double val = readDouble(channel, doubleBuffer);
					m.set(row, col, val);
				}
			}
			return m;
		}
	}

	private static int readInt(FileChannel channel, ByteBuffer buffer)
			throws Exception {
		channel.read(buffer);
		buffer.flip();
		int i = buffer.getInt();
		buffer.clear();
		return i;
	}

	private static double readDouble(FileChannel channel, ByteBuffer buffer)
			throws Exception {
		channel.read(buffer);
		buffer.flip();
		double d = buffer.getDouble();
		buffer.clear();
		return d;
	}
}
