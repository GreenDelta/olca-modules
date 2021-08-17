package org.openlca.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
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
	public static byte[] gzip(byte[] bytes) {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		var bout = new ByteArrayOutputStream();
		try (var gzip = new GZIPOutputStream(bout)) {
			gzip.write(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return bout.toByteArray();
	}

	/**
	 * Returns true if the given byte array has a valid gzip file header
	 * (see https://www.ietf.org/rfc/rfc1952.txt, section 2.3.1)
	 */
	public static boolean isGzip(byte[] bytes) {
		if (bytes == null || bytes.length < 10)
			return false;
		return (bytes[0] & 0xFF) == 31
				&& (bytes[1] & 0xFF) == 139;
	}

	/**
	 * Decompresses the given byte array in the GZIP file format.
	 */
	public static byte[] gunzip(byte[] bytes) {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		var bin = new ByteArrayInputStream(bytes);
		try (var gzip = new GZIPInputStream(bin);
			 var bout = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[4096];
			int count;
			while ((count = gzip.read(buffer)) >= 0)
				bout.write(buffer, 0, count);
			return bout.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Compresses the given byte array using the standard Java deflater (which uses
	 * the zlib compression library internally).
	 */
	public static byte[] zip(byte[] bytes) {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		var deflater = new Deflater();
		deflater.setInput(bytes);
		deflater.finish();
		try (var out = new ByteArrayOutputStream(bytes.length)) {
			byte[] buffer = new byte[4096];
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				out.write(buffer, 0, count);
			}
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Decompresses the given byte array using the standard Java inflater (which
	 * uses the zlib compression library internally).
	 */
	public static byte[] unzip(byte[] bytes) {
		if (bytes == null)
			return null;
		if (bytes.length == 0)
			return new byte[0];
		var inflater = new Inflater();
		inflater.setInput(bytes);
		try(var out = new ByteArrayOutputStream(bytes.length)) {
			byte[] buffer = new byte[4096];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				out.write(buffer, 0, count);
			}
			return out.toByteArray();
		} catch (IOException | DataFormatException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] read(InputStream input) throws IOException {
		if (input == null)
			return null;
		byte[] buf = new byte[8192];
		var bout = new ByteArrayOutputStream(8192);
		int n;
		while ((n = input.read(buf)) > 0) {
			bout.write(buf, 0, n);
		}
		return bout.toByteArray();
	}
}
