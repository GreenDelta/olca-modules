package org.openlca.core.library;

import org.openlca.core.database.IDatabase;

/**
 * Mounts a library on a database. This imports the library meta-data into the
 * database and adds a link to the library in the database.
 */
public class LibraryImport implements Runnable {

	public final IDatabase db;
	public final Library library;

	public LibraryImport(IDatabase db, Library library) {
		this.db = db;
		this.library = library;
	}

	@Override
	public void run() {
	}
}
