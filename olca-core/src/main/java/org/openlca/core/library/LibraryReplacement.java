package org.openlca.core.library;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.Table;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			shouldCompress |= replacer.hasDeleted;
		}
		if (shouldCompress && db instanceof Derby derby) {
			derby.compress();
		}
	}

	private static class BlankReplacer {

		private final Logger log = LoggerFactory.getLogger(getClass());
		private final IDatabase db;
		private final Library lib;
		private final String libId;
		private boolean hasDeleted;

		private BlankReplacer(IDatabase db, Library lib) {
			this.db = db;
			this.lib = lib;
			this.libId = lib.id();
			hasDeleted = false;
		}

		static BlankReplacer of(IDatabase db, Library lib) {
			return new BlankReplacer(db, lib);
		}

		void exec() {
			try (var zip = lib.openJsonZip()) {
				int total = 0;
				for (var type : ModelType.values()) {
					if (!type.isRoot())
						continue;
					var refIds = new HashSet<>(zip.getRefIds(type));
					if (refIds.isEmpty())
						continue;
					total += switch(type) {
						case PROCESS -> updateProcessesOf(refIds);
						case IMPACT_CATEGORY -> updateImpactsOf(refIds);
						default -> updateAllOf(type, refIds);
					};
				}
				if (total > 0) {
					db.addLibrary(lib.id());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private int updateAllOf(ModelType type, Set<String> ids) {
			var clazz = type.getModelClass();
			if (clazz == null)
				return 0;
			var table = clazz.getAnnotation(Table.class);
			if (table == null) {
				log.warn("no @Table annotation found in class {}", clazz);
				return 0;
			}
			log.info("update table: " + table.name());
			var sql = "select ref_id, library from " + table.name();
			var count = new AtomicInteger(0);
			NativeSql.on(db).updateRows(sql, r -> {
				var id = r.getString(1);
				if (ids.contains(id)) {
					r.updateString(2, libId);
					r.updateRow();
					count.incrementAndGet();
				}
				return true;
			});
			return count.get();
		}

		private int updateProcessesOf(Set<String> ids) {
			int updated = 0;
			for (var refId : ids) {
				var p = db.get(Process.class, refId);
				if (p == null)
					continue;
				p.library = libId;
				var qRef = p.quantitativeReference;
				if (qRef != null) {
					qRef.amount = ReferenceAmount.get(qRef);
					if (qRef.flow != null) {
						qRef.flowPropertyFactor = qRef.flow.getReferenceFactor();
						qRef.unit = qRef.flow.getReferenceUnit();
					}
					qRef.formula = null;
				}
				p.exchanges.clear();
				p.parameters.clear();
				p.allocationFactors.clear();
				if (qRef != null) {
					p.exchanges.add(qRef);
				}
				db.update(p);
				updated++;
			}
			hasDeleted |= updated > 0;
			return updated;
		}

		private int updateImpactsOf(Set<String> ids) {
			int updated = 0;
			for(var refId : ids) {
				var impact = db.get(ImpactCategory.class, refId);
				if (impact == null)
					continue;
				impact.impactFactors.clear();
				impact.parameters.clear();
				db.update(impact);
				updated++;
			}
			hasDeleted |= updated > 0;
			return updated;
		}
	}
}
