package org.openlca.core.matrix;

import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

public class CalcAllocationFactor {

	private double amount;
	private boolean evaluated;
	private String formula;

	private CalcAllocationFactor(){
	}

	public static CalcAllocationFactor of(String formula, double amount) {
		var factor = new CalcAllocationFactor();
		factor.amount = amount;
		if (Strings.nullOrEmpty(formula)) {
			factor.evaluated = true;
		} else {
			factor.formula = formula;
			factor.evaluated = false;
		}
		return factor;
	}

	public double get(FormulaInterpreter interpreter) {
		if (evaluated)
			return amount;
		if (interpreter == null)
			return amount;
		try {
			amount = interpreter.eval(formula);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to evaluate formula of allocation factor: "
					+ formula);
		}
		evaluated = true;
		return amount;
	}
}
