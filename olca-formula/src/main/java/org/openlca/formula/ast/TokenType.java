package org.openlca.formula.ast;

/**
 * The token types that our lexer produces.
 */
public enum TokenType {

	/**
	 * A decimal number.
	 */
	NUMBER,

	/**
	 * An identifier: a variable or function name.
	 */
	INDENTIFIER,

	/**
	 * Opening parenthesis: '('
	 */
	PAREN_OPEN,

	/**
	 * Closing parenthesis: ')'
	 */
	PAREN_CLOSE,

	/**
	 * The separator of function parameters.
	 */
	SEPARATOR,

	/**
	 * A Boolean or numeric operator.
	 */
	OPERATOR,

	/**
	 * Indicates the end of the token sequence.
	 */
	EOF,

	/**
	 * Indicates a syntax error.
	 */
	ERROR,
}
