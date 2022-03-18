package org.openlca.core.library;

import java.io.File;
import java.util.Optional;

import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.NpyMatrix;
import org.openlca.npy.Array2d;

public enum LibMatrix {

	/**
	 * The technology matrix A.
	 */
	A,

	/**
	 * The intervention matrix B.
	 */
	B,

	/**
	 * The matrix with characterization factors.
	 */
	C,

	/**
	 * The inverse of the technology matrix INV.
	 */
	INV,

	/**
	 * The intensity matrix M.
	 */
	M;

	@Override
	public String toString() {
		return name();
	}

	public void write(Library library, MatrixReader matrix) {
		if (library == null || matrix == null)
			return;
		NpyMatrix.write(library.folder(), name(), matrix);
	}

	public Optional<MatrixReader> readFrom(Library library) {
		return library == null
			? Optional.empty()
			: NpyMatrix.read(library.folder(), name());
	}

	public Optional<double[]> readColumnFrom(Library library, int column) {

			// dense matrix
			var npy = new File(library.folder(), name() + ".npy");
			if (npy.exists()) {
				var col = Array2d.readColumn(npy, column).asDoubleArray();
				return Optional.of(col.data());
			}

			// sparse matrix
			var matrix = readFrom(library).orElse(null);
			return matrix == null
				? Optional.empty()
				: Optional.of(matrix.getColumn(column));
	}

	public Optional<double[]> readDiagonalFrom(Library library) {
		// dense matrix
		var npy = new File(library.folder(), name() + ".npy");
		if (npy.exists()) {
			var diag = Array2d.readDiag(npy).asDoubleArray();
			return Optional.of(diag.data());
		}

		// sparse matrix
		var matrix = readFrom(library).orElse(null);
		return matrix == null
			? Optional.empty()
			: Optional.of(matrix.diag());
	}

	public boolean isPresentIn(Library library) {
		if (library == null)
			return false;
		var npy = new File(library.folder(), name() + ".npy");
		if (npy.exists())
			return true;
		var npz = new File(library.folder(), name() + ".npz");
		return npz.exists();
	}

}
