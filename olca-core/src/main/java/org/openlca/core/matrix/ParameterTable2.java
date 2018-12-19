package org.openlca.core.matrix;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongObjectHashMap;

public class ParameterTable2 {

	/**
	 * The number generators in case the parameter table is build with
	 * uncertainties.
	 */
	private TLongObjectHashMap<Map<String, NumberGenerator>> numberGens;

	private FormulaInterpreter interpreter = new FormulaInterpreter();

	/**
	 * Builds a formula interpreter for the global parameters and the local
	 * parameters of the given contexts (processes or LCIA methods). It also
	 * applies the given parameter redifinitions.
	 */
	public static FormulaInterpreter interpreter(IDatabase db,
			Set<Long> contexts, Collection<ParameterRedef> redefs) {
		ParameterTable2 table = new ParameterTable2();
		try {
			table.scan(db, contexts);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ParameterTable2.class);
			log.error("Failed to scan parameter table", e);
		}
		return table.interpreter;
	}

	public static ParameterTable2 withUncertainties(IDatabase db,
			Set<Long> contexts, Collection<ParameterRedef> redefs) {
		ParameterTable2 table = new ParameterTable2();
		table.numberGens = new TLongObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1L);
		try {
			table.scan(db, contexts);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ParameterTable2.class);
			log.error("Failed to scan parameter table", e);
		}
		return table;
	}

	private void bindRedefs(Collection<ParameterRedef> redefs) {
		if (redefs == null)
			return;
		for (ParameterRedef redef : redefs) {
		}
	}

	private void scan(IDatabase db, Set<Long> contexts) throws Exception {
		String sql = "select scope, f_owner, name, is_input_param, "
				+ "value, formula";
		if (numberGens != null) {
			sql += "distribution_type, parameter1_value, "
					+ "parameter2_value, parameter3_value";
		}
		sql += " from tbl_parameters";
		NativeSql.on(db).query(sql, r -> {
			ParameterScope pscope = pscope(r.getString(1));
			long owner = r.getLong(2);
			if (pscope == ParameterScope.GLOBAL) {
				owner = 0L;
			} else if (!contexts.contains(owner)) {
				return true;
			}
			Scope scope = scope(pscope, owner);
			String name = r.getString(3);
			boolean isInput = r.getBoolean(4);
			if (isInput) {
				scope.bind(name, Double.toString(r.getDouble(5)));
			} else {
				scope.bind(name, r.getString(6));
			}
			if (numberGens != null) {
				NumberGenerator gen = numberGen(r);
				if (gen != null) {
					Map<String, NumberGenerator> m = numberGens.get(owner);
					if (m == null) {
						m = new HashMap<>();
						numberGens.put(owner, m);
					}
					m.put(name, gen);
				}
			}
			return true;
		});
	}

	private ParameterScope pscope(String scopeStr) {
		if (scopeStr == null)
			return ParameterScope.GLOBAL;
		return ParameterScope.valueOf(scopeStr);
	}

	private Scope scope(ParameterScope pscope, long owner) {
		if (pscope == ParameterScope.GLOBAL)
			return interpreter.getGlobalScope();
		Scope scope = interpreter.getScope(owner);
		if (scope == null) {
			scope = interpreter.createScope(owner);
		}
		return scope;
	}

	private NumberGenerator numberGen(ResultSet r) {
		try {
			int idx = r.getInt(7);
			if (r.wasNull())
				return null;
			UncertaintyType type = UncertaintyType.values()[idx];
			switch (type) {
			case LOG_NORMAL:
				return NumberGenerator.logNormal(
						r.getDouble(8),
						r.getDouble(9));
			case NORMAL:
				return NumberGenerator.normal(
						r.getDouble(8),
						r.getDouble(9));
			case TRIANGLE:
				return NumberGenerator.triangular(
						r.getDouble(8),
						r.getDouble(9),
						r.getDouble(10));
			case UNIFORM:
				return NumberGenerator.uniform(
						r.getDouble(8),
						r.getDouble(9));
			default:
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
