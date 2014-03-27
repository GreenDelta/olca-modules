package org.openlca.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

public class BinUtils {

	private final static String ENCODING = "UTF-8";

	public static void writeString(ByteArrayOutputStream bout, String val)
			throws IOException {
		if (val == null) {
			writeInt(bout, -1);
			return;
		}
		if (val.length() == 0) {
			writeInt(bout, 0);
			return;
		}
		byte[] bytes = val.getBytes(ENCODING);
		writeInt(bout, bytes.length);
		bout.write(bytes);
	}

	public static void writeInt(ByteArrayOutputStream bout, int val)
			throws IOException {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(val);
		byte[] bytes = b.array();
		bout.write(bytes);
	}

	public static String readString(ByteArrayInputStream bin)
			throws IOException {
		int length = readInt(bin);
		if (length == -1)
			return null;
		if (length == 0)
			return "";
		byte[] buffer = new byte[length];
		bin.read(buffer);
		return new String(buffer, ENCODING);
	}

	/**
	 * Reads the next 4 bytes from the stream as integer.
	 */
	public static int readInt(ByteArrayInputStream bin) throws IOException {
		byte[] buffer = new byte[4];
		bin.read(buffer);
		return ByteBuffer.wrap(buffer).getInt();
	}

	/**
	 * Compresses the given byte array using the GZIP file format.
	 */
	public static byte[] gzip(byte[] bytes) throws IOException {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(bout)) {
			gzip.write(bytes);
		}
		return bout.toByteArray();
	}

	/**
	 * Decompresses the given byte array in the GZIP file format.
	 */
	public static byte[] gunzip(byte[] bytes) throws IOException {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		byte[] result = null;
		try (GZIPInputStream gzip = new GZIPInputStream(bin);
		     ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = gzip.read(buffer)) >= 0)
				bout.write(buffer, 0, count);
			result = bout.toByteArray();
		}
		return result;
	}

	/**
	 * Compresses the given byte array using the standard Java deflater (which
	 * uses the zlib compression library internally).
	 */
	public static byte[] zip(byte[] bytes) throws IOException {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		Deflater deflater = new Deflater();
		deflater.setInput(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			out.write(buffer, 0, count);
		}
		out.close();
		return out.toByteArray();
	}

	/**
	 * Decompresses the given byte array using the standard Java inflater (which
	 * uses the zlib compression library internally).
	 */
	public static byte[] unzip(byte[] bytes) throws Exception {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		Inflater inflater = new Inflater();
		inflater.setInput(bytes);
		ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			out.write(buffer, 0, count);
		}
		out.close();
		return out.toByteArray();
	}

}
