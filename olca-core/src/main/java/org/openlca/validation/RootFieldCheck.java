package org.openlca.validation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.IDatabase.DataPackages;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.util.Strings;

import jakarta.persistence.Table;

class RootFieldCheck implements Runnable {

	private final Validation v;
	private final Set<String> refIds = new HashSet<>();
	private DataPackages _dataPackages;
	private boolean foundErrors = false;

	RootFieldCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			for (var type : ModelType.values()) {
				if (type == ModelType.PARAMETER) // do not check local
													// parameters!
					continue;
				var clazz = type.getModelClass();
				if (clazz == null)
					continue;
				if (RootEntity.class.isAssignableFrom(clazz)) {
					check(type);
				}
			}
			checkGlobalParameters();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked root entity fields");
			}
		} catch (Exception e) {
			v.error("failed to check basic fields", e);
		} finally {
			v.workerFinished();
		}
	}

	private void check(ModelType type) {
		if (v.wasCanceled())
			return;
		var table = type.getModelClass().getAnnotation(Table.class);
		if (table == null)
			return;
		var sql = "select " +
		/* 1 */ "id, " +
		/* 2 */ "ref_id, " +
		/* 3 */ "name, " +
		/* 4 */ "f_category, " +
		/* 5 */ "data_package from " + table.name();
		NativeSql.on(v.db).query(sql, r -> {
			checkRow(type, r);
			return !v.wasCanceled();
		});
	}

	private void checkGlobalParameters() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
		/* 1 */ "id, " +
		/* 2 */ "ref_id, " +
		/* 3 */ "name, " +
		/* 4 */ "f_category, " +
		/* 5 */ "data_package, " +
		/* 6 */ "f_owner from tbl_parameters";
		NativeSql.on(v.db).query(sql, r -> {
			long owner = r.getLong(6);
			if (r.wasNull() || owner <= 0) {
				checkRow(ModelType.PARAMETER, r);
			}
			return !v.wasCanceled();
		});
	}

	private void checkRow(ModelType type, ResultSet r) throws SQLException {
		long id = r.getLong(1);

		var refId = r.getString(2);
		if (Strings.nullOrEmpty(refId)) {
			v.error(id, type, "has no reference ID");
			foundErrors = true;
		} else if (refIds.contains(refId)) {
			v.error(id, type, "duplicate reference ID: " + refId);
			foundErrors = true;
		} else {
			refIds.add(refId);
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
		} else if (category == id && type == ModelType.CATEGORY) {
			v.error(id, type, "cyclic category reference: @" + id
					+ ", this can be fixed with the following SQL statement: "
					+ "update tbl_categories set f_category = null where id = f_category");
		}

		var dataPackage = r.getString(5);
		if (Strings.notEmpty(dataPackage)) {
			if (!dataPackages().contains(dataPackage)) {
				v.error(id, type, "points to unlinked library @" + dataPackage);
				foundErrors = true;
			}
		}
	}

	private DataPackages dataPackages() {
		if (_dataPackages != null)
			return _dataPackages;
		_dataPackages = v.db.getDataPackages();
		return _dataPackages;
	}
}
