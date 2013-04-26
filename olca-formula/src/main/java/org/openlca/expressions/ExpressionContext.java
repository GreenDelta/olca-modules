package org.openlca.expressions;

/**
 * ExpressionContext is passed to {@link Expression}s during their
 * evaluate-method.
 */
public interface ExpressionContext {

	/**
	 * Returns the value of the named variable, or null if there's no variable
	 * by that name.
	 */
	public Object resolveVariable(String name) throws ExpressionException;

	/**
	 * Get "something" with a certain name. This could be used if functions need
	 * access to external resources, but is not used by the default function
	 * library.
	 */
	public Object get(String name) throws ExpressionException;
}
