package org.openlca.core.library;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Stack;

import gnu.trove.set.hash.TLongHashSet;
import jakarta.persistence.Table;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.util.Strings;

/**
 * Mounts a library and its dependencies on a database.
 */
record Mounter(IDatabase db, Library library) implements Runnable {

	@Override
	public void run() {

		// just a quick way to calculate the topological
		// order of the dependencies
		var stack = new Stack<Library>();
		var queue = new ArrayDeque<Library>();
		queue.add(library);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			stack.push(next);
			queue.addAll(next.getDependencies());
		}

		try {
			var mounted = new HashSet<>(db.getLibraries());
			while (!stack.isEmpty()) {
				var lib = stack.pop();
				var libId = lib.id();
				if (mounted.contains(libId))
					continue;
				var meta = new File(lib.folder(), "meta.zip");
				try (var store = ZipStore.open(meta)) {
					var imp = new JsonImport(store, db);
					imp.setCallback(e -> {
						if (!(e instanceof RootEntity ce))
							return;
						update(ce, libId);
					});
					imp.run();
				}
				db.addLibrary(libId);
				new CategoryTagger(db, libId).run();
				mounted.add(libId);
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to import library", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends RootEntity> void update(T e, String library) {
		e.library = library;
		Daos.base(db, (Class<T>) e.getClass()).update(e);
	}

	/**
	 * Tags categories which are only used in a specific library with the ID of
	 * that library.
	 */
	private record CategoryTagger(IDatabase db, String libId)
		implements Runnable {

		@Override
		public void run() {

			// collect the IDs of categories that are only used by the library and
			// the IDs of categories that are also used by non-library data sets
			var libCategories = new TLongHashSet();
			var nonLibCategories = new TLongHashSet();
			for (var type : ModelType.values()) {
				if (!type.isRoot())
					continue;
				var table = type.getModelClass().getAnnotation(Table.class);
				if (table == null)
					continue;
				var query = "select distinct f_category, library from " + table.name();
				NativeSql.on(db).query(query, r -> {
					var id = r.getLong(1);
					if (id == 0)
						return true;
					var qLib = r.getString(2);

					if (Strings.nullOrEqual(qLib, libId)) {
						if (!nonLibCategories.contains(id)) {
							libCategories.add(id);
						}
					} else {
						if (libCategories.contains(id)) {
							libCategories.remove(id);
							nonLibCategories.add(id);
						}
					}
					return true;
				});
			}

			// tag the categories and their parents
			for (var it = libCategories.iterator(); it.hasNext();) {
				var libCat = db.get(Category.class, it.next());
				while (libCat != null && !nonLibCategories.contains(libCat.id)) {
					libCat.library = libId;
					libCat = db.update(libCat).category;
				}
			}
		}
	}
}
