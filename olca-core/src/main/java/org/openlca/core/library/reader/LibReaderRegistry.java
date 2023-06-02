package org.openlca.core.library.reader;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.LibraryDir;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

	/**
	 * Gets the library reader for the given library ID. If there is no reader
	 * registered for this ID and no library of this ID is present in the
	 * underlying library folder it returns an empty-reader (which responds with
	 * {@code null} and {@code false}). So {@code null} checking of the returned
	 * object is not required, but it is for the methods of that returned reader.
	 */
	public LibReader get(String libraryId) {
		var registered = readers.get(libraryId);
		if (registered != null)
			return registered;
		var lib = libDir.getLibrary(libraryId).orElse(null);
		if (lib == null) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("library '" + libraryId + "' is not registered and " +
					"does not exist in library folder " + libDir.folder());
			var empty = EmptyLibReader.instance();
			readers.put(libraryId, empty);
			return empty;
		}
		var reader = LibReader.of(lib, db)
			.withSolver(solver)
			.create();
		readers.put(libraryId, reader);
		return reader;
	}

	public List<LibReader> readers() {
		return new ArrayList<>(readers.values());
	}

	public void dispose() {
		readers.values().forEach(LibReader::dispose);
		readers.clear();
	}
}
