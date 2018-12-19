package org.openlca.core.matrix;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * A data structure for fast creation of formula interpreters of the parameters
 * in a database.
 */
public class ParameterTable {

	/**
	 * Maps: parameter scope -> parameter name -> parameter cell. The global
	 * scope is indicated by an owner value of 0.
	 */
	private TLongObjectHashMap<Map<String, ParameterCell>> entries;

	/**
	 * Builds a new parameter table. The table contains all global parameters
	 * from the given database and the parameters of the processes and LCIA
	 * methods with the given IDs.
	 */
	public static ParameterTable build(IDatabase db, Set<Long> contexts) {
		return new ParameterTableBuilder().build(db, contexts);
	}

	private ParameterTable() {
		entries = new TLongObjectHashMap<>();
	}

	/**
	 * Creates a new formula interpreter for the parameter values in this table.
	 */
	public FormulaInterpreter createInterpreter() {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		TLongObjectIterator<Map<String, ParameterCell>> it = entries.iterator();
		while (it.hasNext()) {
			it.advance();
			Map<String, ParameterCell> map = it.value();
			for (ParameterCell cell : map.values())
				cell.bindTo(interpreter);
		}
		return interpreter;
	}

	/**
	 * Calculates new random values for the parameters in this table that have
	 * an uncertainty distribution assigned. The method creates a formula
	 * interpreter that is used for the evaluation of the uncertainty parameters
	 * and returned with the new values bound. Thus, the returned interpreter
	 * can be used in calculations.
	 */
	public FormulaInterpreter simulate() {
		FormulaInterpreter interpreter = createInterpreter();
		TLongObjectIterator<Map<String, ParameterCell>> it = entries.iterator();
		while (it.hasNext()) {
			it.advance();
			Map<String, ParameterCell> map = it.value();
			for (ParameterCell cell : map.values()) {
				cell.eval(interpreter);
				cell.simulate();
				cell.bindTo(interpreter);
			}
		}
		return interpreter;
	}

	/**
	 * Applies the given parameter redefinitions to this table. The respective
	 * parameter values in this table are overwritten by these redefinitions.
	 */
	public void apply(Collection<ParameterRedef> redefs) {
		for (ParameterRedef redef : redefs) {
			Long owner = redef.contextId;
			long scope = owner == null ? 0 : owner;
			Map<String, ParameterCell> map = entries.get(scope);
			if (map == null)
				continue;
			ParameterCell cell = map.get(redef.name);
			if (cell == null)
				continue;
			redefine(cell, redef);
		}
	}

	private void redefine(ParameterCell cell, ParameterRedef redef) {
		cell.generator = null;
		CalcParameter param = cell.param;
		param.value = redef.value;
		param.formula = null; // it is important to delete the formula!
		if (redef.uncertainty == null) {
			param.uncertaintyType = null;
			param.parameter1 = (double) 0;
			param.parameter2 = (double) 0;
			param.parameter3 = (double) 0;
			param.parameter1Formula = null;
			param.parameter2Formula = null;
			param.parameter3Formula = null;
		} else {
			Uncertainty uncertainty = redef.uncertainty;
			param.uncertaintyType = uncertainty.distributionType;
			param.parameter1 = val(uncertainty.parameter1);
			param.parameter2 = val(uncertainty.parameter2);
			param.parameter3 = val(uncertainty.parameter3);
			param.parameter1Formula = uncertainty.formula1;
			param.parameter2Formula = uncertainty.formula2;
			param.parameter3Formula = uncertainty.formula3;
		}
	}

	private double val(Double d) {
		return d == null ? 0 : d;
	}

	void put(CalcParameter param) {
		Map<String, ParameterCell> map = entries.get(param.owner);
		if (map == null) {
			map = new HashMap<>();
			entries.put(param.owner, map);
		}
		ParameterCell cell = new ParameterCell(param);
		map.put(param.name, cell);
	}

	private class ParameterCell {

		private CalcParameter param;
		private NumberGenerator generator;

		ParameterCell(CalcParameter param) {
			this.param = param;
		}

		private String getInterpreterValue() {
			if (param.inputParameter)
				return Double.toString(param.value);
			if (param.formula != null && !param.formula.isEmpty())
				return param.formula;
			else
				return Double.toString(param.value);
		}

		private void simulate() {
			UncertaintyType type = param.uncertaintyType;
			if (type == null || type == UncertaintyType.NONE)
				return;
			if (generator == null)
				generator = createGenerator(type);
			param.value = generator.next();
		}

		private NumberGenerator createGenerator(UncertaintyType type) {
			final CalcParameter p = param;
			switch (type) {
			case LOG_NORMAL:
				return NumberGenerator.logNormal(p.parameter1,
						p.parameter2);
			case NORMAL:
				return NumberGenerator.normal(p.parameter1,
						p.parameter2);
			case TRIANGLE:
				return NumberGenerator.triangular(p.parameter1,
						p.parameter2, p.parameter3);
			case UNIFORM:
				return NumberGenerator.uniform(p.parameter1,
						p.parameter2);
			default:
				return NumberGenerator.discrete(p.value);
			}
		}

		private void bindTo(FormulaInterpreter interpreter) {
			Scope scope = findScope(interpreter);
			scope.bind(param.name, getInterpreterValue());
		}

		private Scope findScope(FormulaInterpreter interpreter) {
			if (param.scope == ParameterScope.GLOBAL)
				return interpreter.getGlobalScope();
			Scope scope = interpreter.getScope(param.owner);
			if (scope == null)
				scope = interpreter.createScope(param.owner);
			return scope;
		}

		void eval(FormulaInterpreter interpreter) {
			if (interpreter == null)
				return;
			try {
				Scope scope = findScope(interpreter);
				tryEval(scope);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("Formula evaluation failed; parameter: "
						+ param.name, e);
			}
		}

		private void tryEval(Scope scope) throws InterpreterException {
			if (param.parameter1Formula != null) {
				double v = scope.eval(param.parameter1Formula);
				param.parameter1 = v;
			}
			if (param.parameter2Formula != null) {
				double v = scope.eval(param.parameter2Formula);
				param.parameter2 = v;
			}
			if (param.parameter3Formula != null) {
				double v = scope.eval(param.parameter3Formula);
				param.parameter3 = v;
			}
		}
	}

	private static class ParameterTableBuilder {

		private Logger log = LoggerFactory.getLogger(getClass());
		private Set<Long> contexts;

		ParameterTable build(IDatabase db, Set<Long> contexts) {
			this.contexts = contexts;
			ParameterTable table = new ParameterTable();
			try {
				putParameters(db, table);
			} catch (Exception e) {
				log.error("error while building parameter table", e);
			}
			this.contexts = null;
			return table;
		}

		private void putParameters(IDatabase db, ParameterTable table)
				throws Exception {
			String query = "select * from tbl_parameters";
			NativeSql.on(db).query(query, r -> {
				CalcParameter param = makeParam(r);
				if (param != null)
					table.put(param);
				return true;
			});
		}

		private CalcParameter makeParam(ResultSet r) {
			try {
				ParameterScope scope = ParameterScope.valueOf(r
						.getString("scope"));
				long owner = r.getLong("f_owner");
				if (scope != ParameterScope.GLOBAL && !contexts.contains(owner))
					return null;
				if (scope == ParameterScope.GLOBAL)
					owner = 0;
				CalcParameter param = new CalcParameter();
				param.name = r.getString("name");
				param.inputParameter = r.getBoolean("is_input_param");
				param.owner = owner;
				param.scope = scope;
				param.value = r.getDouble("value");
				param.formula = r.getString("formula");
				addUncertaintyInfo(r, param);
				return param;
			} catch (Exception e) {
				log.error("failed to get parameter values from db", e);
				return null;
			}
		}

		private void addUncertaintyInfo(ResultSet r, CalcParameter param)
				throws SQLException {
			int uncertaintyType = r.getInt("distribution_type");
			if (!r.wasNull()) {
				param.uncertaintyType = UncertaintyType
						.values()[uncertaintyType];
				param.parameter1 = r.getDouble("parameter1_value");
				param.parameter2 = r.getDouble("parameter2_value");
				param.parameter3 = r.getDouble("parameter3_value");
				param.parameter1Formula = r.getString("parameter1_formula");
				param.parameter2Formula = r.getString("parameter2_formula");
				param.parameter3Formula = r.getString("parameter3_formula");
			}
		}
	}
}
