package org.openlca.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openlca.core.library.Library;
import org.openlca.core.library.LibraryDir;

public final class DataDir {

	/**
	 * The name of the default directory in the users' home folder where we store
	 * our data. `.openLCA` would be probably a much better name and at some point
	 * we may migrate to this but for compatibility reasons we currently stay with
	 * `openLCA-data-1.4`.
	 */
	private static final String DEFAULT_DIR = "openLCA-data-1.4";

	private static File root;

	private DataDir() {
	}

	/**
	 * Returns the root of the openLCA data folder.
	 */
	public static File root() {
		if (root != null)
			return root;
		// since Java 8 `user.home` should really work
		// across all platforms: https://stackoverflow.com/a/586345
		var home = new File(System.getProperty("user.home"));
		var dir = ensureExists(new File(home, DEFAULT_DIR));
		return root = dir;
	}

	/**
	 * Set the given folder as the root folder of the openLCA data directory.
	 */
	public static void setRoot(File dir) {
		root = dir;
	}

	/**
	 * Get the `databases` folder in which the we store our database data where
	 * each databases has its separate folder.
	 */
	public static File databases() {
		return ensureExists(new File(root(), "databases"));
	}

	/**
	 * Get the folder of the database with the given name in the openLCA data
	 * folder. Note that the returned folder may not exist if there is no
	 * such database in that folder.
	 */
	public static File getDatabaseDir(String name) {
		return new File(databases(), name);
	}

	/**
	 * Get the `libraries` folder in which data libraries are stored where each
	 * data library is stored in its separate folder.
	 */
	public static File libraries() {
		return ensureExists(new File(root(), "libraries"));
	}

	public static LibraryDir getLibraryDir() {
		var folder = ensureExists(new File(root(), "libraries"));
		return new LibraryDir(folder);
	}

	public static Library getLibrary(String id) {
		var libDir = getLibraryDir();
		return libDir.getLibrary(id).orElse(null);
	}

	private static File ensureExists(File dir) {
		try {
			Files.createDirectories(dir.toPath());
			return dir;
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to create folder: " + dir.getAbsolutePath(), e);
		}
	}
}
