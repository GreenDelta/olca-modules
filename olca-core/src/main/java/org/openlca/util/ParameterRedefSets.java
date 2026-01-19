package org.openlca.util;

import org.openlca.commons.Strings;
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
	
	/**
	 * Loads the baseline parameter redef set of the product system with the given systemId
	 */
	public static ParameterRedefSet baselineOf(IDatabase db, long systemId) {
		var set = new ParameterRedefSet();
		set.name = "Baseline";
		set.isBaseline = true;
		if (db == null || systemId  == 0l)
			return set;
		
		var sql = "select " +
			/* 1 */ "name, " +
			/* 2 */ "description, " +
			/* 3 */ "value , " +
			/* 4 */ "context_type, " +
			/* 5 */ "f_context from tbl_parameter_redefs where f_owner = " +
			/* 6 */ "(select id from tbl_parameter_redef_sets where is_baseline = 1 " +
			/* 7 */	"and f_product_system = " + systemId + ")";
		
		NativeSql.on(db).query(sql, r -> {
			var redef = new ParameterRedef();
			redef.name = r.getString(1);
			redef.description = r.getString(2);
			redef.value = r.getDouble(3);
			var typeStr = r.getString(4);
			if (Strings.isNotBlank(typeStr)) {
				redef.contextType = ModelType.valueOf(typeStr);
				redef.contextId = r.getLong(5);
			}			
			set.parameters.add(redef);
			return true;
		});

		return set;
	}
	
}
