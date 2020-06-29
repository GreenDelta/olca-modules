package org.openlca.core.library;

import java.io.File;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.input.JsonImport;

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
		try {
			var libID = library.getInfo().id();
			var meta = new File(library.folder, "meta.zip");
			try (var store = ZipStore.open(meta)) {
				JsonImport imp = new JsonImport(store, db);
				imp.setCallback(e -> {
					if (!(e instanceof CategorizedEntity))
						return;
					var ce = (CategorizedEntity) e;
					update(ce, libID);
				});
				imp.run();
			}
			db.addLibrary(libID);
		} catch (Exception e) {
			throw new RuntimeException("failed to import library", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends CategorizedEntity> void update(T e, String library) {
		e.library = library;
		Daos.base(db, (Class<T>) e.getClass()).update(e);
	}
}
