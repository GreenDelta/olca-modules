package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.util.Formula;
import org.openlca.util.ProductSystems;

public class UsedParameters {

	private final IDatabase db;
	private final Set<Long> processes;
	private final List<ScopedFormula> formulas = new ArrayList<>();
	private final Map<Long, Long> contextIds = new HashMap<>();
	private final Map<String, Parameter> parameters = new HashMap<>();
	private final Map<String, ParameterRedef> used = new HashMap<>();
	private final Set<String> checked = new HashSet<>();

	public static List<ParameterRedef> ofSystem(IDatabase db, ProductSystemDescriptor system) {
		if (db == null || system == null)
			return null;
		return new UsedParameters(db, system).get();
	}

	private UsedParameters(IDatabase db, ProductSystemDescriptor system) {
		this.db = db;
		this.processes = ProductSystems.processesOf(db, system);
		loadParameters();
		collectFormulas();
	}

	private List<ParameterRedef> get() {
		parameters.values().stream()
				// process parameters first
				.sorted((p1, p2) -> p1.scope != p2.scope ? p1.scope == ParameterScope.GLOBAL ? 1 : -1 : 0)
				.forEach(param -> {
					if (!param.isInputParameter)
						return;
					if (!isUsed(param))
						return;
					put(param);
				});
		return new ArrayList<>(used.values());
	}

	private void put(Parameter param) {
		if (!param.isInputParameter)
			return;
		var key = keyOf(param);
		used.put(key, redefOf(param, contextIds.get(param.id)));
	}

	private boolean isUsed(Parameter param) {
		var key = keyOf(param);
		if (checked.contains(key))
			return used.containsKey(key);
		checked.add(key);
		for (var formula : formulas) {
			if (formula.matches(param, contextIds.get(param.id))) {
				// check if process parameter is used and not global parameter
				if (param.scope == ParameterScope.GLOBAL && used.containsKey(keyOf(param.name, formula.ownerId)))
					continue;
				if (!formula.isParam())
					return true;
				// check if parameter is used in other parameter formula
				var nextKey = keyOf(formula.parameterName, formula.ownerId);
				var next = parameters.get(nextKey);
				if (next == null)
					continue;
				if (!isUsed(next))
					continue;
				put(param);
				return true;
			}
		}
		return false;
	}

	private String keyOf(Parameter param) {
		if (param.scope == ParameterScope.GLOBAL)
			return param.name;
		return keyOf(param.name, contextIds.get(param.id));
	}

	private String keyOf(String name, Long contextId) {
		if (contextId == null)
			return name;
		return contextId + ":" + name;
	}

	private ParameterRedef redefOf(Parameter param, Long contextId) {
		var redef = ParameterRedef.of(param);
		if (contextId != null) {
			redef.contextType = ModelType.PROCESS;
			redef.contextId = contextId;
		}
		return redef;
	}

	private void loadParameters() {
		var sql = "select " +
		/* 1 */ "id, " +
		/* 2 */ "scope, " +
		/* 3 */ "f_owner, " +
		/* 4 */ "name, " +
		/* 5 */ "is_input_param, " +
		/* 6 */ "value, " +
		/* 7 */ "formula from tbl_parameters";
		NativeSql.on(db).query(sql, r -> {

			var scopeStr = r.getString(2);
			var scope = scopeStr == null
					? ParameterScope.GLOBAL
					: ParameterScope.valueOf(scopeStr);

			if (scope == ParameterScope.IMPACT)
				return true;

			var param = new Parameter();
			param.id = r.getLong(1);
			param.name = r.getString(4);
			param.isInputParameter = r.getBoolean(5);
			param.value = r.getDouble(6);
			param.formula = r.getString(7);
			param.scope = scope;

			var owner = r.getLong(3);
			if (scope == ParameterScope.PROCESS) {
				if (!processes.contains(owner))
					return true;
				contextIds.put(param.id, owner);
			}

			parameters.put(keyOf(param), param);
			return true;
		});

	}

	private void collectFormulas() {
		exchanges();
		allocationFactors();
		parameters();
	}

	private void exchanges() {
		String sql = "SELECT "
				/* 1 */ + "f_owner, "
				/* 2 */ + "resulting_amount_formula, "
				/* 3 */ + "cost_formula FROM tbl_exchanges "
				+ "WHERE resulting_amount_formula IS NOT NULL "
				+ "OR cost_formula IS NOT NULL";
		NativeSql.on(db).query(sql, r -> {
			var ownerId = r.getLong(1);
			if (!processes.contains(ownerId))
				return true;
			var amountFormula = r.getString(2);
			if (Strings.isNotBlank(amountFormula)) {
				formulas.add(new ScopedFormula(amountFormula, ParameterScope.PROCESS, ownerId));
			}
			var costFormula = r.getString(3);
			if (Strings.isNotBlank(costFormula)) {
				formulas.add(new ScopedFormula(costFormula, ParameterScope.PROCESS, ownerId));
			}
			return true;
		});
	}

	private void allocationFactors() {
		var sql = "SELECT "
				/* 1 */ + "f_process, "
				/* 2 */ + "formula FROM tbl_allocation_factors "
				+ "WHERE formula IS NOT NULL";
		NativeSql.on(db).query(sql, r -> {
			var ownerId = r.getLong(1);
			if (!processes.contains(ownerId))
				return true;
			formulas.add(new ScopedFormula(r.getString(2), ParameterScope.PROCESS, ownerId));
			return true;
		});
	}

	private void parameters() {
		var sql = "SELECT "
				/* 1 */ + "f_owner, "
				/* 2 */ + "name, "
				/* 3 */ + "formula FROM tbl_parameters "
				+ "WHERE formula IS NOT NULL AND is_input_param = 0";
		NativeSql.on(db).query(sql, r -> {
			var ownerId = r.getLong(1);
			if (ownerId == 0l) {
				formulas.add(new ScopedFormula(r.getString(3), r.getString(2)));
			}
			if (!processes.contains(ownerId))
				return true;
			formulas.add(new ScopedFormula(r.getString(3), r.getString(2), ParameterScope.PROCESS, ownerId));
			return true;
		});
	}

	private record ScopedFormula(String formula, String parameterName, ParameterScope scope, Long ownerId) {

		private ScopedFormula(String formula, String parameterName) {
			this(formula, parameterName, null, null);
		}

		private ScopedFormula(String formula, ParameterScope scope, long ownerId) {
			this(formula, null, scope, ownerId);
		}

		private boolean matches(Parameter param, Long contextId) {
			if (param.scope != ParameterScope.GLOBAL && scope == ParameterScope.GLOBAL)
				return false;
			if (contextId != null && !contextId.equals(ownerId))
				return false;
			return Formula.matches(formula, param.name);
		}

		private boolean isParam() {
			return parameterName != null;
		}

	}

}
