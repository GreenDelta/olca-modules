package org.openlca.core.library.reader;

import java.io.File;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.NpyMatrix;

/**
 * Contains the file of a matrix, the full matrix, both, or nothing.
 */
record MatrixFile(File file, MatrixReader matrix) {

	private static final MatrixFile _empty = new MatrixFile(null, null);

	/**
	 * Reads the full matrix from the respective file if it exists and
	 * is sparse.
	 */
	static MatrixFile of(Library lib, LibMatrix matrix) {
		if (lib == null || matrix == null)
			return _empty;

		var npy = new File(lib.folder(), matrix.name() + ".npy");
		if (npy.exists())
			return new MatrixFile(npy, null);

		var npz = new File(lib.folder(), matrix.name() + ".npz");
		if (!npz.exists())
			return _empty;
		var m = NpyMatrix.read(npz);
		return new MatrixFile(npz, m);
	}

	/**
	 * Returns {@code true} if it has no matrix and no file.
	 */
	boolean isEmpty() {
		return !hasMatrix() && !hasFile();
	}

	/**
	 * Returns {@code true} when the full matrix is present.
	 */
	boolean hasMatrix() {
		return matrix != null;
	}

	/**
	 * Returns {@code true} when the matrix file exists.
	 */
	boolean hasFile() {
		return file != null && file.exists();
	}
}
