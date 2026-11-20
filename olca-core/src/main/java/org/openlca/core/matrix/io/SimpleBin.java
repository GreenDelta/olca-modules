package org.openlca.core.matrix.io;

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;

/// Reads and writes dense matrices from and to a simple binary format: the
/// number of rows and columns as 32-bit integers, followed by the values in
/// column-major order as 64-bit floating point numbers; all in little-endian
/// byte order.
///
/// The files are memory mapped. The mappings are bound to an arena that is
/// closed at the end of each operation, so that the files are released
/// immediately and can be deleted or renamed afterward, also on Windows.
public class SimpleBin {

	private static final ValueLayout.OfInt INT = ValueLayout.JAVA_INT_UNALIGNED
			.withOrder(ByteOrder.LITTLE_ENDIAN);

	private static final ValueLayout.OfDouble DOUBLE = ValueLayout.JAVA_DOUBLE_UNALIGNED
			.withOrder(ByteOrder.LITTLE_ENDIAN);

	/// The size of the header: rows and columns as 32-bit integers.
	private static final long HEADER = 8;

	public static void write(MatrixReader m, File file) {
		if (m == null || file == null)
			return;

		int rows = m.rows();
		int cols = m.columns();
		long length = HEADER + (long) rows * cols * 8;

		try (var arena = Arena.ofConfined();
				var channel = FileChannel.open(
						file.toPath(), READ, WRITE, CREATE, TRUNCATE_EXISTING)) {

			var seg = channel.map(MapMode.READ_WRITE, 0, length, arena);
			seg.set(INT, 0, rows);
			seg.set(INT, 4, cols);

			// values in column-major order
			long offset = HEADER;
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					seg.set(DOUBLE, offset, m.get(row, col));
					offset += 8;
				}
			}
			seg.force();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to write matrix to file " + file, e);
		}
	}

	public static Matrix read(File file) {
		try (var arena = Arena.ofConfined();
				var channel = FileChannel.open(file.toPath(), READ)) {

			var seg = channel.map(MapMode.READ_ONLY, 0, channel.size(), arena);
			int rows = seg.get(INT, 0);
			int cols = seg.get(INT, 4);
			var m = new DenseMatrix(rows, cols);

			long offset = HEADER;
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					m.set(row, col, seg.get(DOUBLE, offset));
					offset += 8;
				}
			}
			return m;
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to read matrix from " + file, e);
		}
	}
}
