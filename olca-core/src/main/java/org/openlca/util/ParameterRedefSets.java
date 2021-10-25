package org.openlca.util;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.ProductSystem;

public class ParameterRedefSets {

	private ParameterRedefSets() {
	}

	/**
	 * Creates a parameter set with redefinitions of input parameters for global
	 * and process parameters. Only parameters of processes that are part of the
	 * given product system are included.
	 */
	public static ParameterRedefSet allOf(IDatabase db, ProductSystem system) {
		var set = new ParameterRedefSet();
		set.name = "Baseline";
		set.isBaseline = true;
		if (db == null || system == null)
			return set;

		var sql = "select " +
			/* 1 */ "name, " +
			/* 2 */ "description, " +
			/* 3 */ "f_owner, " +
			/* 4 */ "scope, " +
			/* 5 */ "value from tbl_parameters where is_input_param = 1";

		NativeSql.on(db).query(sql, r -> {

			var scopeStr = r.getString(4);
			var scope = scopeStr == null
				? ParameterScope.GLOBAL
				: ParameterScope.valueOf(scopeStr);

			if (scope == ParameterScope.IMPACT)
				return true;

			var redef = new ParameterRedef();
			redef.name = r.getString(1);
			redef.description = r.getString(2);
			redef.value = r.getDouble(5);

			var owner = r.getLong(3);
			if (scope == ParameterScope.PROCESS) {
				if (!system.processes.contains(owner))
					return true;
				redef.contextId = r.getLong(3);
				redef.contextType = ModelType.PROCESS;
			}
			set.parameters.add(redef);
			return true;
		});

		return set;
	}
}
