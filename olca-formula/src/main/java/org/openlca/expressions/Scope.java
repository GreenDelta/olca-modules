package org.openlca.expressions;

import java.io.StringReader;
import java.util.HashMap;

/**
 * A scope contains bindings of variable names to expressions. Each scope has a
 * reference to a parent scope except of the global scope. An expression can be
 * evaluated in a scope.
 *
 */
public final class Scope {

	private final Scope parent;
	private final HashMap<String, Variable> variables = new HashMap<>();
	private final HashMap<String, Integer> evaluationCalls = new HashMap<>();

	Scope() {
		this(null);
	}

	Scope(Scope parent) {
		this.parent = parent;
	}

	/**
	 * Binds the given variable to the given value in this scope.
	 */
	public void bind(String variable, double value) {
		if (variable == null)
			return;
		var symbol = variable.toLowerCase().trim();
		var v = new Variable(symbol, value);
		variables.put(symbol, v);
	}

	/**
	 * Binds the given variable to the given expression in this scope.
	 */
	public void bind(String variable, String expression) {
		if (variable == null || expression == null)
			return;
		var symbol = variable.toLowerCase().trim();
		var v = new Variable(symbol, expression);
		variables.put(symbol, v);
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
		evaluationCalls.clear();
		for (Variable variable : variables.values()) {
			if (variable.expression != null) {
				variable.value = null;
			}
		}
		try {
			return tryEval(expression);
		} catch (Throwable e) {
			throw new InterpreterException("Evaluation of expression "
					+ expression + " failed: " + e.getMessage(), e);
		}
	}

	private double tryEval(String expression) throws Exception {
		var reader = new StringReader(expression.toLowerCase());
		var parser = new FormulaParser(reader);
		parser.parse();
		var e = parser.getExpression();
		e.check();
		var result = e.evaluate(this);
		if (result instanceof Double)
			return (Double) result;
		throw new InterpreterException("The expression " + expression
				+ " does not evaluate to a number.");
	}

	public Object resolveVariable(String name) throws InterpreterException {
		if (name == null)
			return null;
		var symbol = name.trim().toLowerCase();
		var v = variables.get(symbol);
		if (v != null) {
			// variable is bound in this scope
			return v.isEvaluated()
					? v.value
					: eval(v);
		}
		// search in parent scope or constants
		return parent != null
				? parent.resolveVariable(symbol)
				: Constants.get(name);
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

		final String name;
		final String expression;

		private Double value;

		Variable(String name, String expression) {
			this.name = name;
			this.expression = expression;
		}

		Variable(String name, double value) {
			this.name = name;
			this.value = value;
			this.expression = null;
		}

		public boolean isEvaluated() {
			return value != null;
		}
	}
}
