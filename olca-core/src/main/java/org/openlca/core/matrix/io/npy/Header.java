package org.openlca.core.matrix.io.npy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * The header information of an NPY file.
 *
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
		try (FileInputStream fis = new FileInputStream(file)) {
			return read(fis);
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to read header from " + file, e);
		}
	}

	public static Header read(InputStream in) {
		try {
			// The first 6 bytes are a magic string: exactly \x93NUMPY.
			byte[] bytes = new byte[6];
			int n = in.read(bytes);
			if (n != 6) {
				throw new IllegalArgumentException("Not a npy file.");
			}
			String numpy = new String(bytes, 1, 5);
			if (!numpy.equals("NUMPY")) {
				throw new IllegalArgumentException("Not a npy file.");
			}

			// check the version
			int v = in.read();
			if (v < 1) {
				throw new IllegalArgumentException(
						"Invalid npy file, a major version >= 1 is required.");
			}

			// skip the minor version
			in.read();

			// read the header length
			bytes = new byte[2];
			n = in.read(bytes);
			if (n != 2) {
				throw new IllegalArgumentException("Not a npy file.");
			}

			// convert the unsigned short
			ByteBuffer buff = ByteBuffer.wrap(bytes);
			buff.order(ByteOrder.LITTLE_ENDIAN);
			int headerLength = buff.getShort() & 0xffff;

			// read the header string; hoping everything is ASCII
			bytes = new byte[headerLength];
			in.read(bytes);
			Header header = parse(new String(bytes));
			header.dataOffset = headerLength + 10;
			return header;
		} catch (IOException e) {
			throw new RuntimeException("Failed to read header", e);
		}
	}

	static Header parse(String s) {
		Header header = new Header();
		Ref ref = new Ref();
		ref.s = s.trim();
		parseChar(ref, '{');

		for (int i = 0; i < 3; i++) {
			ref.strip();
			String key = parseString(ref);
			ref.strip();
			parseChar(ref, ':');
			ref.strip();
			switch (key) {
				case "descr":
					header.dtype = parseString(ref);
					break;
				case "fortran_order":
					header.fortranOrder = parseBoolean(ref);
					break;
				case "shape":
					header.shape = parseTuple(ref);
					break;
				default:
					throw new IllegalStateException(
							"parsing header failed: bad dictionary key");
			}
			ref.strip();
			if (ref.s.charAt(0) == '}')
				break;
			parseChar(ref, ',');
		}

		ref.strip();
		parseChar(ref, '}');
		ref.strip();
		if (ref.s.length() != 0) {
			throw new IllegalStateException(
					"malformed header");
		}
		return header;
	}

	private static String parseString(Ref ref) {
		parseChar(ref, '\'');
		int i = ref.s.indexOf('\'');
		if (i < 0) {
			throw new IllegalStateException(
					"parsing header failed: malformed string");
		}
		String s = ref.s.substring(0, i);
		ref.s = ref.s.substring(i + 1);
		return s;
	}

	private static boolean parseBoolean(Ref ref) {
		if (ref.s.substring(0, 4).equals("True")) {
			ref.s = ref.s.substring(4);
			return true;
		}
		if (ref.s.substring(0, 5).equals("False")) {
			ref.s = ref.s.substring(5);
			return false;
		}
		throw new IllegalStateException(
				"parsing header failed: excepted True or False");
	}

	private static int parseInt(Ref ref) {
		int len = 0;
		for (int i = 0; i < ref.s.length(); i++) {
			char c = ref.s.charAt(i);
			if (!Character.isDigit(c)) {
				break;
			}
			len++;
		}
		if (len == 0) {
			throw new IllegalStateException(
					"parsing header failed: no digits");
		}
		String intstr = ref.s.substring(0, len);
		ref.s = ref.s.substring(len);
		return Integer.parseInt(intstr);
	}

	private static int[] parseTuple(Ref ref) {
		parseChar(ref, '(');
		List<Integer> tup = new ArrayList<>();
		while (true) {
			ref.strip();
			if (ref.s.charAt(0) == ')')
				break;
			int n = parseInt(ref);
			tup.add(n);
			ref.strip();
			if (ref.s.charAt(0) == ')')
				break;
			parseChar(ref, ',');
		}
		parseChar(ref, ')');

		int[] tuple = new int[tup.size()];
		for (int i = 0; i < tup.size(); i++) {
			tuple[i] = tup.get(i);
		}
		return tuple;
	}

	private static void parseChar(Ref ref, char c) {
		char first = ref.s.charAt(0);
		if (first != c) {
			throw new IllegalStateException(
					"parsing header failed: expected character '"
							+ c + "', found '" + first + "'");
		}
		ref.s = ref.s.substring(1);
	}

	private static class Ref {
		String s;

		void strip() {
			s = s.trim();
		}
	}
}
