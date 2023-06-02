package org.openlca.core.library.reader;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.solvers.MatrixSolver;

import java.util.HashMap;
import java.util.Map;

public class LibReaderRegistry {

	private final LibraryDir libDir;
	private final IDatabase db;
	private final Map<String, LibReader> readers = new HashMap<>();

	private MatrixSolver solver;

	private LibReaderRegistry(LibraryDir libDir, IDatabase db) {
		this.libDir = libDir;
		this.db = db;
	}

	public static LibReaderRegistry of(LibraryDir libDir, IDatabase db) {
		return new LibReaderRegistry(libDir, db);
	}

	public LibReaderRegistry withSolver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	/**
	 * Registers a specific reader for the given library ID. If no
	 * specific reader is registered for a library, a default reader
	 * is loaded for a library.
	 */
	public LibReaderRegistry register(String libraryId, LibReader reader) {
		readers.put(libraryId, reader);
		return this;
	}

	public LibReader get(String libraryId) {
		var registered = readers.get(libraryId);
		if (registered != null)
			return registered;
		var lib = libDir.getLibrary(libraryId).orElse(null);
		if (lib == null) {
			throw new IllegalStateException(
				"library '" + libraryId +
					"' does not exist in library folder " + libDir.folder());
		}
		var reader = LibReader.of(lib, db)
			.withSolver(solver)
			.create();
		readers.put(libraryId, reader);
		return reader;
	}

	public void dispose() {
		readers.values().forEach(LibReader::dispose);
		readers.clear();
	}
}
