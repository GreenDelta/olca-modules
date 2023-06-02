package org.openlca.core.library.reader;

import org.openlca.core.library.LibMatrix;
import org.openlca.core.library.Library;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;

import java.io.IOException;
import java.nio.file.Files;

class EmptyLibReader implements LibReader {

	private static final EmptyLibReader instance = new EmptyLibReader();
	private final Library lib;

	private EmptyLibReader() {
		try {
			var dir = Files.createTempDirectory("_empty_lib" );
			lib = Library.of(dir.toFile());
		} catch (IOException e) {
			throw new RuntimeException(
					"failed to create temp.-dir for empty library", e);
		}
	}

	static LibReader instance() {
		return instance;
	}

	@Override
	public Library library() {
		return lib;
	}

	@Override
	public TechIndex techIndex() {
		return null;
	}

	@Override
	public EnviIndex enviIndex() {
		return null;
	}

	@Override
	public ImpactIndex impactIndex() {
		return null;
	}

	@Override
	public MatrixReader matrixOf(LibMatrix matrix) {
		return null;
	}

	@Override
	public double[] costs() {
		return new double[0];
	}

	@Override
	public double[] diagonalOf(LibMatrix matrix) {
		return new double[0];
	}

	@Override
	public double[] columnOf(LibMatrix matrix, int col) {
		return new double[0];
	}
}
