package org.openlca.core.math;

import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Variable;

public class FormulaInterpreter {

	private FormulaInterpreter interpreter = new FormulaInterpreter();

	public void bind(String parameter, String formula) {
		Variable var = new Variable(parameter, formula);
		interpreter.bind(var);
	}

	public double eval(String expression) throws InterpreterException {
		return interpreter.eval(expression);
	}
}
