package org.openlca.core.library;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Retagger {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final Library lib;
	private final String libId;
	private boolean hasDeleted;

	private Retagger(IDatabase db, Library lib) {
		this.db = db;
		this.lib = lib;
		this.libId = lib.name();
		hasDeleted = false;
	}

	static Retagger of(IDatabase db, Library lib) {
		return new Retagger(db, lib);
	}

	public boolean hasDeleted() {
		return hasDeleted;
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
				total += switch (type) {
					case PROCESS -> updateProcessesOf(refIds);
					case IMPACT_CATEGORY -> updateImpactsOf(refIds);
					default -> updateAllOf(type, refIds);
				};
			}
			if (total > 0) {
				db.addLibrary(lib.name());
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
		for (var refId : ids) {
			var impact = db.get(ImpactCategory.class, refId);
			if (impact == null)
				continue;
			impact.library = libId;
			impact.impactFactors.clear();
			impact.parameters.clear();
			db.update(impact);
			updated++;
		}
		hasDeleted |= updated > 0;
		return updated;
	}
}
