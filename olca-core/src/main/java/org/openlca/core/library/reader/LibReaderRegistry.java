package org.openlca.core.library.reader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;
import org.slf4j.LoggerFactory;

public class LibReaderRegistry {

	private final Map<String, LibReader> readers = new HashMap<>();

	private LibReaderRegistry() {
	}

	/**
	 * Creates a registry of (default) library readers for the matrix libraries
	 * linked to the given database.
	 */
	public static LibReaderRegistry of(IDatabase db, LibraryDir libDir) {
		var reg = new LibReaderRegistry();
		var mounted = db.getLibraries().stream()
				.map(libDir::getLibrary)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();
		var queue = new ArrayDeque<>(mounted);
		while (!queue.isEmpty()) {
			var lib = queue.poll();
			if (lib.hasMatrices()) {
				var reader = LibReader.of(lib, db).create();
				reg.readers.put(lib.name(), reader);
			}
			lib.getDirectDependencies().stream()
					.filter(dep ->
							!reg.readers.containsKey(dep.name())
							&& !queue.contains(dep))
					.forEach(queue::add);
		}
		return reg;
	}

	public static LibReaderRegistry of(IDatabase db, Library lib) {
		var reg = new LibReaderRegistry();
		var reader = LibReader.of(lib, db).create();
		reg.readers.put(lib.name(), reader);
		return reg;
	}

	/**
	 * Creates a registry for the given library readers.
	 */
	public static LibReaderRegistry of(Iterable<LibReader> readers) {
		var reg = new LibReaderRegistry();
		for (var r : readers) {
			reg.readers.put(r.libraryName(), r);
		}
		return reg;
	}

	/**
	 * Gets the library reader for the given library ID. If there is no reader
	 * registered for this ID it returns an empty-reader (which responds with
	 * {@code null} and {@code false}). So {@code null} checking of the returned
	 * object is not required, but it is for the methods of that returned reader.
	 */
	public LibReader get(String libraryId) {
		var registered = readers.get(libraryId);
		if (registered != null)
			return registered;
		var log = LoggerFactory.getLogger(getClass());
		log.error("library '{}' is not registered", libraryId);
		var empty = EmptyLibReader.instance();
		readers.put(libraryId, empty);
		return empty;
	}

	public List<LibReader> readers() {
		return new ArrayList<>(readers.values());
	}

	public void dispose() {
		readers.values().forEach(LibReader::dispose);
		readers.clear();
	}
}
