package org.openlca.core.matrix;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
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

/**
 * Contains the parameters of a set of processes or impact assessment methods
 * that can be bound to a formula interpreter which then can be used in
 * calculations.
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
	public static ParameterTable build(IDatabase database, Set<Long> contexts) {
		return new ParameterTableBuilder().build(database, contexts);
	}

	ParameterTable() {
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
			Long owner = redef.getContextId();
			long scope = owner == null ? 0 : owner;
			Map<String, ParameterCell> map = entries.get(scope);
			if (map == null)
				continue;
			ParameterCell cell = map.get(redef.getName());
			if (cell == null)
				continue;
			redefine(cell, redef);
		}
	}

	private void redefine(ParameterCell cell, ParameterRedef redef) {
		cell.generator = null;
		CalcParameter param = cell.param;
		param.setValue(redef.getValue());
		param.setFormula(null); // it is important to delete the formula!
		if (redef.getUncertainty() == null) {
			param.setUncertaintyType(null);
			param.setParameter1(0);
			param.setParameter2(0);
			param.setParameter3(0);
			param.setParameter1Formula(null);
			param.setParameter2Formula(null);
			param.setParameter3Formula(null);
		} else {
			Uncertainty uncertainty = redef.getUncertainty();
			param.setUncertaintyType(uncertainty.getDistributionType());
			param.setParameter1(val(uncertainty.getParameter1Value()));
			param.setParameter2(val(uncertainty.getParameter2Value()));
			param.setParameter3(val(uncertainty.getParameter3Value()));
			param.setParameter1Formula(uncertainty.getParameter1Formula());
			param.setParameter2Formula(uncertainty.getParameter2Formula());
			param.setParameter3Formula(uncertainty.getParameter3Formula());
		}
	}

	private double val(Double d) {
		return d == null ? 0 : d;
	}

	void put(CalcParameter param) {
		Map<String, ParameterCell> map = entries.get(param.getOwner());
		if (map == null) {
			map = new HashMap<>();
			entries.put(param.getOwner(), map);
		}
		ParameterCell cell = new ParameterCell(param);
		map.put(param.getName(), cell);
	}

	private class ParameterCell {

		private CalcParameter param;
		private NumberGenerator generator;

		ParameterCell(CalcParameter param) {
			this.param = param;
		}

		private String getInterpreterValue() {
			if (param.isInputParameter())
				return Double.toString(param.getValue());
			if (param.getFormula() != null && !param.getFormula().isEmpty())
				return param.getFormula();
			else
				return Double.toString(param.getValue());
		}

		private void simulate() {
			UncertaintyType type = param.getUncertaintyType();
			if (type == null || type == UncertaintyType.NONE)
				return;
			if (generator == null)
				generator = createGenerator(type);
			param.setValue(generator.next());
		}

		private NumberGenerator createGenerator(UncertaintyType type) {
			final CalcParameter p = param;
			switch (type) {
			case LOG_NORMAL:
				return NumberGenerator.logNormal(p.getParameter1(),
						p.getParameter2());
			case NORMAL:
				return NumberGenerator.normal(p.getParameter1(),
						p.getParameter2());
			case TRIANGLE:
				return NumberGenerator.triangular(p.getParameter1(),
						p.getParameter2(), p.getParameter3());
			case UNIFORM:
				return NumberGenerator.uniform(p.getParameter1(),
						p.getParameter2());
			default:
				return NumberGenerator.discrete(p.getValue());
			}
		}

		private void bindTo(FormulaInterpreter interpreter) {
			Scope scope = findScope(interpreter);
			scope.bind(param.getName(), getInterpreterValue());
		}

		private Scope findScope(FormulaInterpreter interpreter) {
			if (param.getScope() == ParameterScope.GLOBAL)
				return interpreter.getGlobalScope();
			Scope scope = interpreter.getScope(param.getOwner());
			if (scope == null)
				scope = interpreter.createScope(param.getOwner());
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
				log.error(
						"Formula evaluation failed; parameter: "
								+ param.getName(), e);
			}
		}

		private void tryEval(Scope scope) throws InterpreterException {
			if (param.getParameter1Formula() != null) {
				double v = scope.eval(param.getParameter1Formula());
				param.setParameter1(v);
			}
			if (param.getParameter2Formula() != null) {
				double v = scope.eval(param.getParameter2Formula());
				param.setParameter2(v);
			}
			if (param.getParameter3Formula() != null) {
				double v = scope.eval(param.getParameter3Formula());
				param.setParameter3(v);
			}
		}
	}
}
