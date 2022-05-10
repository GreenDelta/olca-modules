package org.openlca.validation;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;

final class Util {

	private Util() {
	}

	static FormulaInterpreter interpreterOf(IDatabase db) {
		var interpreter = new FormulaInterpreter();
		var sql = "select " +
			/* 1 */ "scope, " +
			/* 2 */ "f_owner, " +
			/* 3 */ "name, " +
			/* 4 */ "is_input_param, " +
			/* 5 */ "value," +
			/* 6 */ "formula from tbl_parameters";
		NativeSql.on(db).query(sql, r -> {

			// parse the parameter scope
			var _str = r.getString(1);
			var paramScope = _str == null
				? ParameterScope.GLOBAL
				: ParameterScope.valueOf(_str);

			// get the interpreter scope
			long owner = r.getLong(2);
			if (paramScope == ParameterScope.GLOBAL) {
				owner = 0L;
			}
			var scope = owner == 0
				? interpreter.getGlobalScope()
				: interpreter.getOrCreate(owner);

			// bind the parameter value or formula
			var name = r.getString(3);
			boolean isInput = r.getBoolean(4);
			if (isInput) {
				// value
				scope.bind(name, r.getDouble(5));
			} else {
				// formula
				scope.bind(name, r.getString(6));
			}

			return true;
		});
		return interpreter;
	}

}
