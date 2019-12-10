package org.openlca.core.matrix.io.npy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public static Header read(InputStream stream) {
		return HeaderReader.read(stream);
	}

	public static Header read(String s) {
		return HeaderReader.parse(s);
	}

}
