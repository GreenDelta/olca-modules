package org.openlca.expressions;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

/**
 * A scope contains bindings of variable names to expressions. Each scope has a
 * reference to a parent scope except of the global scope. An expression can be
 * evaluated in a scope. If the
 * 
 */
public final class Scope {

	private final Scope parent;
	private final HashMap<String, Integer> evaluationCalls = new HashMap<>();
	private final HashMap<String, Variable> variables = new HashMap<>();

	Scope() {
		this(null);
	}

	Scope(Scope parent) {
		this.parent = parent;
	}

	/**
	 * Creates a new binding of a variable name to an expression in this scope.
	 */
	public void bind(String variableName, String expression) {
		if (variableName == null || expression == null)
			return;
		String symbol = variableName.toLowerCase().trim();
		Variable var = new Variable();
		var.name = symbol;
		var.expression = expression;
		variables.put(symbol, var);
	}

	/**
	 * Removes all variable bindings from the scope.
	 */
	public void clear() {
		variables.clear();
		evaluationCalls.clear();
	}

	/**
	 * Evaluates the given expression in this scope.
	 */
	public double eval(String expression) throws InterpreterException {
		// reset the evaluation calls and values for variables
		for (String var : evaluationCalls.keySet())
			evaluationCalls.put(var, 0);
		for (Variable variable : variables.values())
			variable.value = null;
		try {
			return tryEval(expression);
		} catch (Throwable e) {
			throw new InterpreterException("Evaluation of expression "
					+ expression + " failed: " + e.getMessage(), e);
		}
	}

	private double tryEval(String expression) throws Exception {
		Reader reader = new StringReader(expression.toLowerCase());
		FormulaParser parser = new FormulaParser(reader);
		parser.parse();
		Expression e = parser.getExpression();
		e.check();
		Object result = e.evaluate(this);
		if (!(result instanceof Double))
			throw new InterpreterException("The given expression " + expression
					+ " does not evaluate to a number.");
		return ((Double) result);
	}

	public Object resolveVariable(String name) throws InterpreterException {
		Variable var = variables.get(name);
		if (var != null) {
			// variable is bound in this scope
			if (var.isEvaluated())
				return var.value;
			else
				return eval(var);
		} else {
			// search in parent scope or constants
			if (parent == null)
				return Constants.get(name);
			else {
				return parent.resolveVariable(name);
			}
		}
	}

	private Object eval(Variable var) throws InterpreterException {
		Integer call = evaluationCalls.get(var.name);
		if (call != null && call > 0)
			throw new InterpreterException(
					"Second evaluation call on variable "
							+ var.name + ". Cyclic dependencies?");
		evaluationCalls.put(var.name, 1);
		try {
			var.value = tryEval(var.expression);
			return var.value;
		} catch (Throwable e) {
			throw new InterpreterException("Evaluation of variable "
					+ var.name + " failed: " + e.getMessage(), e);
		}
	}

	private class Variable {

		private String name;
		private String expression;
		private Double value;

		public boolean isEvaluated() {
			return value != null;
		}
	}
}