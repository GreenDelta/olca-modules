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
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A data structure for fast creation of formula interpreters for the parameters
 * of a database.
 */
public class ParameterTable {

	/**
	 * The number generators in case the parameter table is build with
	 * uncertainties. Maps: parameter scope -> parameter name -> number generator.
	 * The global scope is indicated by a key of 0L.
	 */
	private TLongObjectHashMap<Map<String, NumberGenerator>> numberGens;

	private final FormulaInterpreter interpreter = new FormulaInterpreter();

	private ParameterTable() {
	}

	/**
	 * Builds a formula interpreter for the global parameters and the local
	 * parameters of the given contexts (processes or LCIA methods). It also applies
	 * the given parameter redefinitions.
	 */
	public static FormulaInterpreter interpreter(IDatabase db,
			Set<Long> contexts, Collection<ParameterRedef> redefs) {
		var table = new ParameterTable();
		try {
			table.scan(db, contexts);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(ParameterTable.class);
			log.error("Failed to scan parameter table", e);
		}
		table.bindRedefs(redefs);
		return table.interpreter;
	}

	/**
	 * Builds a parameter table suitable for creating formula interpreters in a
	 * Monte Carlo simulation.
	 */
	public static ParameterTable forSimulation(IDatabase db,
			Set<Long> contexts, Collection<ParameterRedef> redefs) {
		var table = new ParameterTable();
		table.numberGens = new TLongObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1L);
		try {
			table.scan(db, contexts);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(ParameterTable.class);
			log.error("Failed to scan parameter table", e);
		}
		table.bindRedefs(redefs);
		return table;
	}

	/**
	 * Calculates new random values for the parameters in this table that have an
	 * uncertainty distribution assigned. It re-binds the values of theses
	 * parameters in the underlying interpreter with the generated values.
	 */
	public FormulaInterpreter simulate() {
		if (numberGens == null)
			return interpreter;
		var it = numberGens.iterator();
		while (it.hasNext()) {
			it.advance();
			long context = it.key();
			var generators = it.value();
			var scope = context == 0
					? interpreter.getGlobalScope()
					: interpreter.getScopeOrGlobal(context);
			if (generators == null || scope == null)
				continue;
			generators.forEach((name, gen) -> {
				if (gen != null) {
					scope.bind(name, gen.next());
				}
			});
		}
		return interpreter;
	}

	private void bindRedefs(Collection<ParameterRedef> redefs) {
		if (redefs == null)
			return;
		for (var redef : redefs) {
			var scope = redef.contextId == null
					? interpreter.getGlobalScope()
					: interpreter.getScopeOrGlobal(redef.contextId);
			scope.bind(redef.name, redef.value);
			if (numberGens == null)
				continue;

			long context = redef.contextId != null ? redef.contextId : 0L;
			var generators = numberGens.get(context);
			if (generators == null) {
				if (redef.uncertainty == null)
					continue;
				generators = new HashMap<>();
				numberGens.put(context, generators);
			}
			if (redef.uncertainty == null
					|| redef.uncertainty.distributionType == UncertaintyType.NONE) {
				generators.remove(redef.name);
				continue;
			}
			generators.put(redef.name, redef.uncertainty.generator());
		}
	}

	private void scan(IDatabase db, Set<Long> contexts) {
		String sql = "select scope, f_owner, name, is_input_param, "
				+ "value, formula";
		if (numberGens != null) {
			sql += ", distribution_type, parameter1_value, "
					+ "parameter2_value, parameter3_value";
		}
		sql += " from tbl_parameters";
		NativeSql.on(db).query(sql, r -> {

			// parse the parameter scope
			var _str = r.getString(1);
			var paramScope = _str == null
					? ParameterScope.GLOBAL
					: ParameterScope.valueOf(_str);

			// load the scope
			long owner = r.getLong(2);
			if (paramScope == ParameterScope.GLOBAL) {
				owner = 0L;
			} else if (!contexts.contains(owner)) {
				return true;
			}
			var scope = owner == 0
					? interpreter.getGlobalScope()
					: interpreter.getOrCreate(owner);

			// bind the parameter value or formula
			var name = r.getString(3);
			boolean isInput = r.getBoolean(4);
			if (isInput) {
				scope.bind(name, r.getDouble(5));
			} else {
				scope.bind(name, r.getString(6));
			}

			// bind a possible number generator
			if (numberGens != null) {
				var generator = numberGen(r);
				if (generator != null) {
					var m = numberGens.get(owner);
					if (m == null) {
						m = new HashMap<>();
						numberGens.put(owner, m);
					}
					m.put(name, generator);
				}
			}
			return true;
		});
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
