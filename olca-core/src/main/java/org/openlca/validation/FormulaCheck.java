package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.commons.Strings;

import java.util.HashSet;
import java.util.Locale;
import java.util.function.Supplier;

class FormulaCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;
	private FormulaInterpreter interpreter;

	FormulaCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			interpreter = Util.interpreterOf(v.db);
			checkParameterNames();
			checkParameterFormulas();
			checkExchangeFormulas();
			checkAllocationFormulas();
			checkImpactFormulas();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked formulas");
			}
		} catch (Exception e) {
			v.error("error in formula validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkParameterFormulas() {
		if (v.wasCanceled())
			return;

		var sql = "select " +
				/* 1 */ "id, " +
				/* 2 */ "name, " +
				/* 3 */ "scope, " +
				/* 4 */ "f_owner, " +
				/* 5 */ "is_input_param, " +
				/* 6 */ "formula from tbl_parameters";

		NativeSql.on(v.db).query(sql, r -> {

			boolean isInput = r.getBoolean(5);
			if (isInput)
				return !v.wasCanceled();

			long paramId = r.getLong(1);

			// parse the parameter scope
			var _str = r.getString(3);
			var paramScope = _str == null
					? ParameterScope.GLOBAL
					: ParameterScope.valueOf(_str);
			var formula = r.getString(6);

			// check formulas of global parameters
			if (paramScope == ParameterScope.GLOBAL) {
				if (Strings.isBlank(formula)) {
					v.error(paramId, ModelType.PARAMETER, "empty formula");
					foundErrors = true;
					return !v.wasCanceled();
				}
				try {
					interpreter.getGlobalScope().eval(formula);
				} catch (Exception e) {
					v.error(paramId, ModelType.PARAMETER,
							"formula error in '" + formula + "': " + e.getMessage());
					foundErrors = true;
				}
				return !v.wasCanceled();
			}

			// check formulas of local parameters
			var paramName = r.getString(2);
			var modelId = r.getLong(4);
			var modelType = paramScope == ParameterScope.IMPACT
					? ModelType.IMPACT_CATEGORY
					: ModelType.PROCESS;

			if (Strings.isBlank(formula)) {
				v.error(modelId, modelType, "empty formula of parameter '"
						+ paramName + "'");
				foundErrors = true;
				return !v.wasCanceled();
			}

			check(modelId, modelType, formula, () -> String.format(
					"error in formula '%s' of parameter '%s'", formula, paramName));

			return !v.wasCanceled();

		});
	}

	private void checkExchangeFormulas() {
		if (v.wasCanceled())
			return;

		var sql = "select " +
				/* 1 */ "f_owner, " +
				/* 2 */ "resulting_amount_formula, " +
				/* 3 */ "cost_formula from tbl_exchanges";

		NativeSql.on(v.db).query(sql, r -> {
			long ownerId = r.getLong(1);

			var amountFormula = r.getString(2);
			check(ownerId, ModelType.PROCESS, amountFormula,
					() -> "error in exchange formula '" + amountFormula + "'");

			var costFormula = r.getString(3);
			check(ownerId, ModelType.PROCESS, costFormula,
					() -> "error in cost formula '" + costFormula + "'");

			return !v.wasCanceled();
		});
	}

	private void checkAllocationFormulas() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "f_process, " +
				/* 2 */ "formula from tbl_allocation_factors";
		NativeSql.on(v.db).query(sql, r -> {
			long processId = r.getLong(1);
			var formula = r.getString(2);
			check(processId, ModelType.PROCESS, formula,
					() -> "error in allocation formula '" + formula + "'");
			return !v.wasCanceled();
		});
	}

	private void checkImpactFormulas() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "f_impact_category, " +
				/* 2 */ "formula from tbl_impact_factors";
		NativeSql.on(v.db).query(sql, r -> {
			long impactId = r.getLong(1);
			var formula = r.getString(2);
			check(impactId, ModelType.IMPACT_CATEGORY, formula,
					() -> "error in factor formula '" + formula + "'");
			return !v.wasCanceled();
		});
	}

	private void check(long modelId, ModelType modelType, String formula,
										 Supplier<String> message) {
		if (Strings.isBlank(formula))
			return;
		try {
			var scope = interpreter.getScopeOrGlobal(modelId);
			scope.eval(formula);
		} catch (Exception e) {
			v.error(modelId, modelType, message.get() + ": " + e.getMessage());
			foundErrors = true;
		}
	}

	private void checkParameterNames() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "id, " +
				/* 2 */ "name, " +
				/* 3 */ "scope, " +
				/* 4 */ "f_owner from tbl_parameters";

		var keys = new HashSet<>();

		NativeSql.on(v.db).query(sql, r -> {

			// check if there is an error
			String error = null;
			var name = r.getString(2);
			var owner = r.getLong(4);
			if (Strings.isBlank(name)) {
				error = "parameter with empty name";
				//} else if (!Parameters.isValidName(name)) { // TODO too heavy currently
				//	error = "invalid parameter name: '" + name + "'";
			} else {
				var key = name.trim().toLowerCase(Locale.US) + "#" + owner;
				if (keys.contains(key)) {
					error = "duplicate parameter name '" + name + "'";
				} else {
					keys.add(key);
				}
			}
			if (error == null)
				return !v.wasCanceled();

			// map the error to the parameter scope
			var _str = r.getString(3);
			var paramScope = _str == null
					? ParameterScope.GLOBAL
					: ParameterScope.valueOf(_str);
			switch (paramScope) {
				case GLOBAL -> v.error(r.getLong(1), ModelType.PARAMETER, error);
				case PROCESS -> v.error(owner, ModelType.PROCESS, error);
				case IMPACT -> v.error(owner, ModelType.IMPACT_CATEGORY, error);
			}
			foundErrors = true;
			return !v.wasCanceled();
		});

	}
}
