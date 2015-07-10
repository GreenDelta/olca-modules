package org.openlca.core.matrix.io;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import org.openlca.core.math.IMatrix;
import org.openlca.core.math.IMatrixFactory;

/**
 * Reads a binary Matlab file (version?) with a matrix with 64 bit floating
 * point numbers into a IMatrix.
 */
public class MatBinMatrixReader {

	/**
	 * Number of bytes per number in matrix (8 bytes == double).
	 */
	private final int SIZE = 8;

	private final File file;
	private final IMatrixFactory<?> factory;

	private boolean useStreaming = false;
	private ByteBuffer buffer;

	public MatBinMatrixReader(File file, IMatrixFactory<?> factory) {
		this.file = file;
		this.factory = factory;
		buffer = ByteBuffer.allocate(SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public void setUseStreaming(boolean useStreaming) {
		this.useStreaming = useStreaming;
	}

	public IMatrix read() throws Exception {
		if (useStreaming)
			return readViaStreaming();
		else
			return readInMemory();
	}

	private IMatrix readInMemory() throws Exception {
		byte[] bytes = Files.readAllBytes(file.toPath());
		checkFormat(bytes);
		int rows = (int) readNumber(3, bytes);
		int cols = (int) readNumber(4, bytes);
		IMatrix matrix = factory.create(rows, cols);
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				int offset = 5 + row + col * matrix.getRowDimension();
				double val = readNumber(offset, bytes);
				matrix.setEntry(row, col, val);
			}
		}
		return matrix;
	}

	private IMatrix readViaStreaming() throws Exception {
		try (FileInputStream fis = new FileInputStream(file);
				FileChannel channel = fis.getChannel()) {
			checkFormat(channel);
			int rows = (int) readNumber(channel); // pos = 3
			int cols = (int) readNumber(channel); // pos = 4
			IMatrix matrix = factory.create(rows, cols);
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					double val = readNumber(channel);
					matrix.setEntry(row, col, val);
				}
			}
			return matrix;
		}
	}

	private void checkFormat(FileChannel channel) throws Exception {
		int dims = (int) readNumber(channel); // pos = 0
		int isSparse = (int) readNumber(channel); // pos = 1
		int size = (int) readNumber(channel); // pos = 2
		checkFormat(dims, isSparse, size);
	}

	private void checkFormat(byte[] bytes) throws Exception {
		int dims = (int) readNumber(0, bytes);
		int isSparse = (int) readNumber(1, bytes);
		int size = (int) readNumber(2, bytes);
		checkFormat(dims, isSparse, size);
	}

	private void checkFormat(int dims, int isSparse, int size) {
		// number of dimensions
		if (dims != 2)
			throw new IllegalStateException("Number of dimensions must be 2; " +
					"but was " + dims);
		// sparse flag (1 if sparse)
		if (isSparse != 0)
			throw new IllegalStateException("Can only read dense matrices; " +
					"but the matrix is indicated as sparse " + isSparse);
		// number precision in bytes
		if (size != SIZE)
			throw new IllegalStateException(
					"The number precision does not match; " +
							"expected " + SIZE
							+ " byte numbers; but precision is " + size
							+ " bytes");
	}

	private double readNumber(int i, byte[] bytes) {
		buffer.put(bytes, i * SIZE, SIZE);
		buffer.flip();
		double d = buffer.getDouble();
		buffer.compact();
		return d;
	}

	private double readNumber(FileChannel channel) throws Exception {
		channel.read(buffer);
		buffer.flip();
		double d = buffer.getDouble();
		buffer.compact();
		return d;
	}
}
