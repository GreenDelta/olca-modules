package org.openlca.expressions;

import java.io.StringReader;
import java.util.HashMap;

public class Interpreter {

	private InterpreterContext context;
	private HashMap<String, Variable> vars;

	public Interpreter() {
		vars = new HashMap<>();
		context = new InterpreterContext(this, vars);
	}

	public void bind(Variable var) {
		vars.put(var.getName().toLowerCase(), var);
	}

	public void bindAndEval(Variable var) throws InterpreterException {
		bind(var);
		eval(var);
	}

	public double eval(String expression) throws InterpreterException {
		FormulaParser parser = new FormulaParser(new StringReader(
				expression.toLowerCase()));
		try {
			return tryEval(expression, parser);
		} catch (Exception e) {
			throw new InterpreterException("Evaluation of expression "
					+ expression + " failed", e);
		}
	}

	private double tryEval(String expression, FormulaParser parser)
			throws Exception {
		parser.parse();
		Expression e = parser.getExpression();
		e.check();
		Object result = e.evaluate(context);
		if (!(result instanceof Double))
			throw new InterpreterException("The given expression " + expression
					+ " does not evaluate to a number.");
		return ((Double) result);
	}

	public void evalVariables() throws InterpreterException {
		context.reset();
		for (Variable var : vars.values()) {
			eval(var);
		}
	}

	void eval(Variable var) throws InterpreterException {
		if (var.getExpression() != null)
			var.setValue(eval(var.getExpression()));
	}
}
