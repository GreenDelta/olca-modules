package org.openlca.validation;

import java.util.Set;

import jakarta.persistence.Table;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class RootFieldCheck implements Runnable {

	private final Validation v;
	private Set<String> _libs;
	private boolean foundErrors = false;

	RootFieldCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			for (var type : ModelType.values()) {
				var clazz = type.getModelClass();
				if (clazz == null)
					continue;
				if (CategorizedEntity.class.isAssignableFrom(clazz)) {
					check(type);
				}
			}
			if (!foundErrors && !v.hasStopped()) {
				v.ok("checked root entity fields");
			}
		} catch (Exception e) {
			v.error("failed to check basic fields", e);
		} finally {
			v.workerFinished();
		}
	}

	private void check(ModelType type) {
		if (v.hasStopped())
			return;
		var table = type.getModelClass().getAnnotation(Table.class);
		if (table == null)
			return;

		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "ref_id, " +
			/* 3 */ "name, " +
			/* 4 */ "f_category, " +
			/* 5 */ "library from " + table.name();
		NativeSql.on(v.db).query(sql, r -> {
			long id = r.getLong(1);

			var refID = r.getString(2);
			if (Strings.nullOrEmpty(refID)) {
				v.error(id, type, "has no reference ID");
				foundErrors = true;
			}

			var name = r.getString(3);
			if (Strings.nullOrEmpty(name)) {
				v.warning(id, type, "has an empty name");
				foundErrors = true;
			}

			var category = r.getLong(4);
			if (category != 0
					&& !v.ids.contains(ModelType.CATEGORY, category)) {
				v.error(id, type, "invalid category link @" + category);
				foundErrors = true;
			}

			var library = r.getString(5);
			if (Strings.notEmpty(library)) {
				if (!libraries().contains(library)) {
					v.error(id, type, "points to unlinked library @" + library);
					foundErrors = true;
				}
			}
			return !v.hasStopped();
		});
	}

	private Set<String> libraries() {
		if (_libs != null)
			return _libs;
		_libs = v.db.getLibraries();
		return _libs;
	}

}
