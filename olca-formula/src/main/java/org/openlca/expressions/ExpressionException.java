// ported from the xReporter project
package org.openlca.expressions;

public class ExpressionException extends Exception {

	private static final long serialVersionUID = -4703871530196236790L;

	protected int line;
	protected int column;

	public ExpressionException(String message, int line, int column) {
		super(message);
		this.line = line;
		this.column = column;
	}

	public ExpressionException(String message) {
		super(message);
	}

	@Override
	public String getMessage() {
		return super.getMessage() + " at line " + line + ", column " + column;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
