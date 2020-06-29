package org.openlca.core.library;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A library directory is a specific folder where each sub-folder is a library.
 * If a library A in that folder has a dependency to a library B there should be
 * a sub-folder with that library B in the same directory. The identifier of a
 * library, which is the combination of name and version, is used as the folder
 * name of a library.
 */
public class LibraryDir {

	public final File dir;

	public LibraryDir(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("the folder " + dir
						+ " does not exist and could not be created");
			}
		}
		this.dir = dir;
	}

	public List<Library> getLibraries() {
		var files = dir.listFiles();
		if (files == null)
			return Collections.emptyList();
		return Arrays.stream(files)
				.filter(dir -> dir.isDirectory()
						&& new File(dir, "library.json").exists())
				.map(Library::new)
				.collect(Collectors.toList());
	}

	/**
	 * Gets the library for the given ID if it exists in this library folder.
	 */
	public Optional<Library> get(String id) {
		if (id == null)
			return Optional.empty();
		var folder = new File(dir, id);
		if (!folder.exists())
			return Optional.empty();
		var meta = new File(folder, "library.json");
		return meta.exists()
				? Optional.of(new Library(folder))
				: Optional.empty();
	}

	/**
	 * Get the folder of the library with the given meta-data in this library
	 * directory. This folder may not exist yet.
	 */
	public File getFolder(LibraryInfo info) {
		return new File(dir, info.id());
	}

	/**
	 * Returns true if a library with the given meta-data exists.
	 */
	public boolean exists(LibraryInfo info) {
		return getFolder(info).exists();
	}
}
