package org.openlca.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openlca.core.database.Derby;
import org.openlca.core.library.LibraryDir;

public record DataDir(File root) {

	/**
	 * The name of the default directory in the users' home folder where we store
	 * our data. `.openLCA` would be probably a much better name and at some point
	 * we may migrate to this but for compatibility reasons we currently stay with
	 * `openLCA-data-1.4`.
	 */
	private static final String DEFAULT_DIR = "openLCA-data-1.4";

	public DataDir(File root) {
		this.root = ensureExists(root);
	}

	public static DataDir get(File root) {
		return new DataDir(root);
	}

	public static DataDir get() {
		// since Java 8 `user.home` should really work
		// across all platforms: https://stackoverflow.com/a/586345
		var home = new File(System.getProperty("user.home"));
		var dir = new File(home, DEFAULT_DIR);
		return new DataDir(dir);
	}

	/**
	 * Returns the folder where the local databases are stored.
	 */
	public File getDatabasesDir() {
		return ensureExists(new File(root(), "databases"));
	}

	/**
	 * Get the folder of the database with the given name in an openLCA data
	 * folder. Note that the returned folder may not exist if there is no such
	 * database in that folder.
	 */
	public File getDatabaseDir(String name) {
		return new File(getDatabasesDir(), name);
	}

	/**
	 * Get the `libraries` folder in which data libraries are stored where each
	 * data library is stored in its separate folder.
	 */
	public LibraryDir getLibraryDir() {
		var folder = ensureExists(new File(root(), "libraries"));
		return new LibraryDir(folder);
	}

	/**
	 * Creates or opens a local database with the given name in the database
	 * folder.
	 */
	public Derby openDatabase(String name) {
		var dbDir = new File(getDatabasesDir(), name);
		return new Derby(dbDir);
	}

	private static File ensureExists(File dir) {
		if (dir.exists())
			return dir;
		try {
			Files.createDirectories(dir.toPath());
			return dir;
		} catch (IOException e) {
			throw new RuntimeException(
				"Failed to create folder: " + dir.getAbsolutePath(), e);
		}
	}
}
