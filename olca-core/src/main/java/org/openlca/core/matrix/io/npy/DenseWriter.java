package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.DenseMatrix;
import org.openlca.core.matrix.format.IMatrix;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicReference;

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

			// allocate the byte buffer and write the header to it
			AtomicReference<MappedByteBuffer> ref = new AtomicReference<>();
			head.write(hsize -> {
				// convert to 64 bit integers to avoid number overflows
				long bufSize = ((long) hsize)
						+ (((long) matrix.rows())
						* ((long) matrix.columns())
						* 8L);
				MappedByteBuffer buf = channel.map(
						FileChannel.MapMode.READ_WRITE, 0L, bufSize);
				buf.order(ByteOrder.LITTLE_ENDIAN);
				ref.set(buf);
				return buf;
			});
			MappedByteBuffer buf = ref.get();

			// write the matrix data
			if (matrix instanceof DenseMatrix) {
				DenseMatrix dmatrix = (DenseMatrix) matrix;
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
