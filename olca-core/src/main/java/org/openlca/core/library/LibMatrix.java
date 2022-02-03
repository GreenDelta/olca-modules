package org.openlca.core.library;

import java.util.Optional;

import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.io.NpyMatrix;

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
		NpyMatrix.write(library.folder, name(), matrix);
	}

	public Optional<MatrixReader> read(Library library) {
		return library == null
			? Optional.empty()
			: NpyMatrix.read(library.folder, name());
	}

}
