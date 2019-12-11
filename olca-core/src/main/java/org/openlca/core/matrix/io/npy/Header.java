package org.openlca.core.matrix.io.npy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;

/**
 * The header information of an NPY file.
 * <p>
 * see https://numpy.org/devdocs/reference/generated/numpy.lib.format.html
 */
public class Header {

	public String dtype;
	public boolean fortranOrder;
	public int[] shape;

	/**
	 * Contains the number of bytes from the beginning of the file to the
	 * position where the data section starts. The offset includes the magic
	 * string, the version fields, the header length, and the header.
	 */
	int dataOffset;

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("{'descr': '");
		b.append(dtype).append("', 'fortran_order': ");
		if (fortranOrder) {
			b.append("True");
		} else {
			b.append("False");
		}
		b.append(", 'shape': (");
		if (shape != null) {
			for (int i : shape) {
				b.append(i).append(',');
			}
		}
		b.append("), }");
		return b.toString();
	}

	public static Header read(File file) {
		try (FileInputStream fis = new FileInputStream(file);
			 BufferedInputStream buf = new BufferedInputStream(fis, 16)) {
			return read(buf);
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to read header from " + file, e);
		}
	}

	/**
	 * Read the header information from an NPY file. It will not close the
	 * given stream and the position of the stream will be directly after the
	 * header so that it can be used for further reading.
	 */
	public static Header read(InputStream stream) {
		return HeaderReader.read(stream);
	}

	public static Header read(String s) {
		return HeaderReader.parse(s);
	}

	public DType getDType() {
		return DType.fromString(dtype);
	}

	/**
	 * Try to derive the byte order from the Numpy dtype of this
	 * header; otherwise returns little endian order by default.
	 */
	public ByteOrder getByteOrder() {
		if (dtype == null)
			return ByteOrder.LITTLE_ENDIAN;
		if (dtype.startsWith(">"))
			return ByteOrder.BIG_ENDIAN;
		return ByteOrder.LITTLE_ENDIAN;
	}

	/**
	 * Writes this header to the given output stream including
	 * padding as described in the NPY format so that it can
	 * be followed by the data.
	 */
	void write(OutputStream out) {
		try {
			AtomicReference<ByteBuffer> ref = new AtomicReference<>();
			write(headerLen -> {
				ByteBuffer buffer = ByteBuffer.allocate(headerLen);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				ref.set(buffer);
				return buffer;
			});
			out.write(ref.get().array());
		} catch (Exception e) {
			throw new RuntimeException("failed to write header " + this, e);
		}
	}

	/**
	 * Writes this header to the byte buffer returned by the given function. The
	 * required size for the header (including padding etc.) will be passed to
	 * that function which must return a ByteBuffer with at least that size.
	 * Also, the byte order of that buffer must be little endian order as we
	 * write all of our data in that order.
	 */
	void write(IntFunction<ByteBuffer> buffer) {
		try {
			byte[] headerBytes = (this.toString()).getBytes(
					StandardCharsets.US_ASCII);

			// see https://docs.scipy.org/doc/numpy-1.14.2/neps/npy-format.html
			// 6 bytes: “x93NUMPY”
			// 1 byte: major version
			// 1 byte: minor version
			// 2 bytes: header length (as unsigned short)
			// h bytes: header
			// 1 byte: '\n'
			// + padding so that it is divisible by 16
			int unpadded = 10 + headerBytes.length + 1;
			int padding = 0;
			if ((unpadded % 16) != 0) {
				padding = 16 - (unpadded % 16);
			}

			// get the byte buffer
			ByteBuffer buf = buffer.apply(unpadded + padding);

			// magic
			buf.put((byte) 0x93);
			buf.put("NUMPY".getBytes());
			// major version
			buf.put((byte) 0x01);
			// minor version
			buf.put((byte) 0x00);
			// header length
			buf.putShort((short) ((headerBytes.length + 1 + padding) & 0xffff));

			// header & padding
			buf.put(headerBytes);
			for (int i = 0; i < padding; i++) {
				buf.put((byte)' ');
			}
			buf.put((byte)'\n');
		} catch (Exception e) {
			throw new RuntimeException("failed to write header " + this, e);
		}
	}
}
