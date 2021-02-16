package org.openlca.core.matrix.io;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;

public class SimpleBin {

	public static void write(MatrixReader m, File file) {
		if (m == null || file == null)
			return;

		int length = 8 + m.rows() * m.columns() * 8;

		try (var raf = new RandomAccessFile(file, "rw");
				var channel = raf.getChannel()) {

			var buffer = channel.map(MapMode.READ_WRITE, 0, length);
			buffer.order(ByteOrder.LITTLE_ENDIAN);

			// rows + columns
			buffer.putInt(m.rows());
			buffer.putInt(m.columns());

			// values
			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					buffer.putDouble(m.get(row, col));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to write matrix to file " + file, e);
		}

	}

	public static Matrix read(File file) {
		try (var raf = new RandomAccessFile(file, "r");
				var channel = raf.getChannel()) {

			var buffer = channel.map(MapMode.READ_ONLY, 0, raf.length());
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int rows = buffer.getInt();
			int cols = buffer.getInt();
			var m = new DenseMatrix(rows, cols);

			for (int col = 0; col < m.columns(); col++) {
				for (int row = 0; row < m.rows(); row++) {
					double val = buffer.getDouble();
					m.set(row, col, val);
				}
			}
			return m;
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to read matrix from " + file, e);
		}
	}
}
