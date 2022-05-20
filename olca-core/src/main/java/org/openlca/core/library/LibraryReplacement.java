package org.openlca.core.library;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.Table;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

/**
 * First simple version of a library replacement. Currently, it just sets
 * library links for matching data sets in a database.
 */
public class LibraryReplacement implements Runnable {

	private final IDatabase db;
	private Library target;

	private LibraryReplacement(IDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public static LibraryReplacement of(IDatabase db) {
		return new LibraryReplacement(db);
	}

	public LibraryReplacement withTarget(Library lib) {
		this.target = lib;
		return this;
	}

	@Override
	public void run() {
		if (target == null)
			return;
		boolean shouldCompress = false;
		for (var lib : Libraries.dependencyOrderOf(target)) {
			var replacer = BlankReplacer.of(db, lib);
			replacer.exec();
			shouldCompress |= replacer.hasDeleted.get();
		}
		if (shouldCompress && db instanceof Derby derby) {
			derby.compress();
		}
	}

	private record BlankReplacer(
		IDatabase db, Library lib, AtomicBoolean hasDeleted) {

		static BlankReplacer of(IDatabase db, Library lib) {
			return new BlankReplacer(db, lib, new AtomicBoolean(false));
		}

		void exec() {
			try (var zip = lib.openJsonZip()) {
				int total = 0;
				for (var type : ModelType.values()) {
					if (!type.isRoot())
						continue;
					var libIds = new HashSet<>(zip.getRefIds(type));
					var clazz = type.getModelClass();
					var table = clazz.getAnnotation(Table.class);
					if (table == null)
						continue;
					var count = tag(table.name(), libIds);
					total += count;
				}
				if (total > 0) {
					db.addLibrary(lib.id());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private int tag(String table, Set<String> libIds) {
			var sql = "select ref_id, library from " + table;
			var libId = lib.id();
			var count = new AtomicInteger(0);
			NativeSql.on(db).updateRows(sql, r -> {
				var id = r.getString(1);
				if (libIds.contains(id)) {
					r.updateString(2, libId);
					r.updateRow();
					count.incrementAndGet();
				}
				return true;
			});
			return count.get();
		}
	}
}
