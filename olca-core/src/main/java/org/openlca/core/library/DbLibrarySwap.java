package org.openlca.core.library;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.reader.LibReader;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.ZipReader;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

/// Tries to replace a background system of a database with a library.
public class DbLibrarySwap implements Runnable {

	private final IDatabase db;
	private final LibReader lib;

	public DbLibrarySwap(IDatabase db, LibReader lib) {
		this.db = db;
		this.lib = lib;
	}

	@Override
	public void run() {
		try {
			var libId = lib.libraryName();
			var meta = new File(lib.library().folder(), "meta.zip");
			try (var zip = ZipReader.of(meta)) {
				var imp = new JsonImport(zip, db);
				imp.setUpdateMode(UpdateMode.ALWAYS);
				imp.setCallback(e -> {
					if (e instanceof RootEntity ce) {
						ce.library = libId;
						db.update(ce);
					}
				});
				imp.run();
			}
			db.clearCache();
			LibraryLinkCleanup.of(db).run();
			db.addLibrary(libId);
		} catch (Exception e) {
			throw new RuntimeException("failed to add library", e);
		}
	}
}
