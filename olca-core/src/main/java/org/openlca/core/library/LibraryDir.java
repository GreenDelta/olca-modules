package org.openlca.core.library;

import org.openlca.util.Dirs;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A library directory is a specific folder where each sub-folder is a library.
 * If a library A in that folder has a dependency to a library B there should be
 * a sub-folder with that library B in the same directory. The identifier of a
 * library, which is typically a combination of the library name and version, is
 * used as the name of the folder of a library.
 */
public record LibraryDir(File folder) {

	public static LibraryDir of(File dir) {
		return new LibraryDir(dir);
	}

	public LibraryDir {
		Dirs.createIfAbsent(folder);
	}

	public List<Library> getLibraries() {
		var files = folder.listFiles();
		if (files == null)
			return Collections.emptyList();
		return Arrays.stream(files)
			.filter(File::isDirectory)
			.map(Library::new)
			.collect(Collectors.toList());
	}

	/**
	 * Gets the library for the given ID if it exists in this library folder.
	 */
	public Optional<Library> getLibrary(String id) {
		if (id == null)
			return Optional.empty();
		var folder = new File(folder(), id);
		return folder.exists()
			? Optional.of(new Library(folder))
			: Optional.empty();
	}

	public boolean hasLibrary(String id) {
		return getLibrary(id).isPresent();
	}

	/**
	 * Initializes a new library with the given name. If a library with this
	 * name already exists, it will return that library.
	 */
	public Library create(String name) {
		Objects.requireNonNull(name);
		var lib = getLibrary(name);
		if (lib.isPresent())
			return lib.get();

		var newLib = Library.of(new File(folder, name));
		// write library info
		LibraryInfo.of(name).writeTo(newLib);
		return newLib;
	}
}
