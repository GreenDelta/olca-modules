package org.openlca.core.matrix.io.npy;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.IMatrix;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

	private static CSCMatrix readCSC(ZipFile zip) throws IOException {
		int[] shape;
		try (InputStream buff = buff(zip, "shape.npy")) {
			shape = Npy.readIntVector(buff);
		}
		if (shape.length < 2) {
			throw new IllegalStateException("shape is < 2");
		}
		double[] values;
		try (InputStream buff = buff(zip, "data.npy")) {
			values = Npy.readVector(buff);
		}
		int[] columnPointers;
		try (InputStream buff = buff(zip, "indptr.npy")) {
			columnPointers = Npy.readIntVector(buff);
		}
		int[] rowIndices;
		try (InputStream buff = buff(zip, "indices.npy")) {
			rowIndices = Npy.readIntVector(buff);
		}
		return new CSCMatrix(shape[0], shape[1], values,
				columnPointers, rowIndices);
	}

	/**
	 * Returns a buffered input stream of the entry with the given name. An
	 * {@link IllegalStateException} is thrown when there is no such entry in
	 * the zip file.
	 */
	private static InputStream buff(ZipFile zip, String name) throws IOException {
		ZipEntry e = zip.getEntry(name);
		if (e == null) {
			throw new IllegalStateException(
					"the zip file " + zip + " does not contain an entry " + name);
		}
		return new BufferedInputStream(zip.getInputStream(e));
	}

	public static void save(File file, CSCMatrix m) {
		if (file == null || m == null)
			return;
		try (FileOutputStream out = new FileOutputStream(file);
			 BufferedOutputStream buffer = new BufferedOutputStream(out);
			 ZipOutputStream zip = new ZipOutputStream(buffer)) {
			zip.setLevel(ZipOutputStream.STORED);

			// shape
			ZipEntry shape = new ZipEntry("shape.npy");
			zip.putNextEntry(shape);
			Npy.write(zip, new int[]{m.rows, m.columns});
			zip.closeEntry();



		} catch (IOException e) {
			throw new RuntimeException("failed to create zip: " + file, e);
		}
	}


	public static void main(String[] args) {
		String path = "/Users/ms/Downloads/csc.npz";
		IMatrix m = Npz.load(new File(path));
		System.out.println(m.rows() + " * " + m.columns());
	}

}
