package org.openlca.expressions;

import java.util.HashMap;

public class FormulaInterpreter {

	private final Scope globalScope;
	private final HashMap<Long, Scope> scopes = new HashMap<>();

	public FormulaInterpreter() {
		globalScope = new Scope();
	}

	/**
	 * Removes all local scopes and all variable bindings of the global scope from
	 * this interpreter.
	 */
	public void clear() {
		globalScope.clear();
		scopes.clear();
	}

	/**
	 * Evaluates the given expression in the global scope of the interpreter.
	 */
	public double eval(String expression) throws InterpreterException {
		return globalScope.eval(expression);
	}

	/**
	 * Binds the given variable to the given expression in the global scope of the
	 * interpreter.
	 */
	public void bind(String variable, String expression) {
		getGlobalScope().bind(variable, expression);
	}

	/**
	 * Binds the given variable to the given value in the global scope of the
	 * interpreter.
	 */
	public void bind(String variable, double value) {
		getGlobalScope().bind(variable, value);
	}

	/** Returns the global scope of the interpreter. */
	public Scope getGlobalScope() {
		return globalScope;
	}

	/** Creates a new scope with the given ID in the global scope. */
	public Scope createScope(long id) {
		return createScope(id, globalScope);
	}

	/** Creates a new scope with the given ID in the parent scope. */
	public Scope createScope(long id, Scope parent) {
		Scope scope = new Scope(parent);
		scopes.put(id, scope);
		return scope;
	}

	/** Returns true if the interpreter has a scope with the given ID. */
	public boolean hasScope(long id) {
		return scopes.containsKey(id);
	}

	/** Returns the scope with the given ID or null if no such scope is defined. */
	public Scope getScope(long id) {
		return scopes.get(id);
	}

}
