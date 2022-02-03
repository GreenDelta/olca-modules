package org.openlca.core.library;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openlca.core.DataDir;
import org.openlca.core.matrix.format.MatrixReader;

/**
 * A library directory is a specific folder where each sub-folder is a library.
 * If a library A in that folder has a dependency to a library B there should be
 * a sub-folder with that library B in the same directory. The identifier of a
 * library, which is the combination of name and version, is used as the folder
 * name of a library.
 */
public class LibraryDir {

	public final File dir;

	public static LibraryDir getDefault() {
		return new LibraryDir(DataDir.libraries());
	}

	public static LibraryDir of(File dir) {
		return new LibraryDir(dir);
	}

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

	/**
	 * Initializes a new library folder for the given library information. If
	 * this library already exists, it returns that library.
	 */
	public Library init(LibraryInfo info) {
		if (exists(info))
			return get(info.id()).orElseThrow();
		var dir = getFolder(info);
		try {
			if (!dir.exists()) {
				Files.createDirectories(dir.toPath());
			}
			var lib = new Library(dir);
			info.writeTo(lib);
			return lib;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create library folder", e);
		}
	}

	public Optional<MatrixReader> getMatrix(String libID, LibMatrix matrix) {
		var lib = get(libID);
		if (lib.isEmpty())
			return Optional.empty();
		return lib.get().getMatrix(matrix);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var other = (LibraryDir) o;
		return Objects.equals(dir, other.dir);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dir);
	}
}
