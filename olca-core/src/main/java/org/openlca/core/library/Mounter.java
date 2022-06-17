package org.openlca.core.library;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.util.Strings;

import gnu.trove.set.hash.TLongHashSet;
import jakarta.persistence.Table;

/**
 * Mounts a library and its dependencies on a database.
 */
public class Mounter implements Runnable {

	private final IDatabase db;
	private final Library library;
	private final Map<Library, MountAction> actions = new HashMap<>();

	private Mounter(IDatabase db, Library library) {
		this.db = db;
		this.library = library;
	}

	public static Mounter of(IDatabase db, Library library) {
		return new Mounter(db, library);
	}

	public Mounter apply(Map<Library, MountAction> actions) {
		if (actions != null) {
			this.actions.putAll(actions);
		}
		return this;
	}

	public Mounter applyDefaultsOf(PreMountCheck.Result result) {
		if (result == null || result.isError())
			return this;
		result.getStates().forEach(pair -> {
			var lib = pair.first;
			var state = pair.second;
			if (state != null) {
				actions.put(lib, state.defaultAction());
			}
		});
		return this;
	}

	@Override
	public void run() {
		try {
			boolean shouldCompress = false;
			for (var lib : Libraries.dependencyOrderOf(library)) {
				var action = actions.getOrDefault(lib, MountAction.UPDATE);
				if (action == MountAction.SKIP)
					continue;
				if (action == MountAction.RETAG) {
					var retagger = Retagger.of(db, lib);
					retagger.exec();
					shouldCompress = retagger.hasDeleted();
				} else {
					mount(lib);
				}
				new CategoryTagger(db, lib.name()).run();
			}
			if (db instanceof Derby derby && shouldCompress) {
				derby.compress();
			}
			db.clearCache();
		} catch (Exception e) {
			throw new RuntimeException("failed to import library", e);
		}
	}

	private void mount(Library lib) throws IOException {
		try (var zip = lib.openJsonZip()) {
			new JsonImport(zip, db)
				.setUpdateMode(UpdateMode.ALWAYS)
				.run();
			for (var type : ModelType.values()) {
				if (!type.isRoot())
					continue;
				var refIds = zip.getRefIds(type);
				if (refIds.isEmpty())
					continue;
				tag(lib.name(), type, new HashSet<>(refIds));
			}
			db.addLibrary(lib.name());
		}
	}

	private void tag(String libId, ModelType type, Set<String> refIds) {
		var clazz = type.getModelClass();
		if (clazz == null)
			return;
		var table = clazz.getAnnotation(Table.class);
		var sql = "select ref_id, library from " + table.name();
		NativeSql.on(db).updateRows(sql, r -> {
			var id = r.getString(1);
			if (refIds.contains(id)) {
				r.updateString(2, libId);
				r.updateRow();
			}
			return true;
		});
	}

	/**
	 * Tags categories which are only used in a specific library with the ID of
	 * that library.
	 */
	@Deprecated
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
			for (var it = libCategories.iterator(); it.hasNext(); ) {
				var libCat = db.get(Category.class, it.next());
				while (libCat != null && !nonLibCategories.contains(libCat.id)) {
					libCat.library = libId;
					libCat = db.update(libCat).category;
				}
			}
		}
	}
}
