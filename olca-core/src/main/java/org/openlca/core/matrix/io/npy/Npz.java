package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.IMatrix;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A NPZ file is just a zip file of NPY files. In SciPy, sparse matrices can
 * be stored in NPZ files. We support some reading and writing of some of these
 * matrix types from and to NPZ files here; namely:
 *
 * <ul>
 *     <li>CSC matrices (compressed sparse column matrices)</li>
 *     <li>TODO other ...</li>
 * </ul>
 * <p>
 * see https://docs.scipy.org/doc/scipy/reference/sparse.html
 */
public final class Npz {

	private Npz() {
	}

	public static IMatrix load(File file) {
		try (ZipFile zip = new ZipFile(file)) {
			String format = getFormat(zip);
			if (format == null)
				throw new IllegalArgumentException(
						"unsupported NPZ file; no format entry");
			switch (format) {
				case "csc":
					return readCSC(zip);
				default:
					throw new IllegalArgumentException(
							"unsupported format: " + format);
			}

		} catch (IOException e) {
			throw new RuntimeException("failed to read zip: " + file, e);
		}
	}

	/**
	 * Returns a list with the entries of the given NPZ file. As an NPZ file
	 * is just a zip file of NPY files this gives the names of the NPY files
	 * in the zip.
	 */
	public static List<String> entries(File file) {
		try (ZipFile zip = new ZipFile(file)) {
			List<String> names = new ArrayList<>();
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();
				names.add(e.getName());

				// TODO: this is currently just for testing
				System.out.println(e.getName());
				try (InputStream in = zip.getInputStream(e);
					 BufferedInputStream buf = new BufferedInputStream(in)) {
					System.out.println(Header.read(buf));
				}
			}
			return names;
		} catch (IOException e) {
			throw new RuntimeException("Failed to read NPZ file " + file, e);
		}
	}

	private static String getFormat(ZipFile zip) throws IOException {
		ZipEntry ze = zip.getEntry("format.npy");
		if (ze == null)
			return null;
		try (InputStream in = zip.getInputStream(ze);
			 BufferedInputStream buf = new BufferedInputStream(in, 16)) {
			Header h = Header.read(buf);
			if (h.dtype == null || !h.dtype.contains("S"))
				return null;
			// "S" means null-terminated string
			// there should be only a few bytes that indicate the format
			StringBuilder f = new StringBuilder();
			int next;
			while ((next = buf.read()) > 0) {
				f.append((char) next);
			}
			return f.toString();
		}
	}

	private static CSCMatrix readCSC(ZipFile zip) {
		return null;
	}

	public static void main(String[] args) {
		String path = "/Users/ms/Downloads/csc.npz";
		System.out.println(Npz.load(new File(path)));
	}

}
