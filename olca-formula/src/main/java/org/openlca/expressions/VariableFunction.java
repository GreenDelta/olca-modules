// ported from the xReporter project
package org.openlca.expressions;

class VariableFunction implements Expression {

	protected String variableName;
	protected String position;
	protected int line, column;

	public VariableFunction(String variableName) {
		this.variableName = variableName;
	}

	@Override
	public void addArgument(Expression function) {
		throw new RuntimeException("Cannot add an argument to a variable");
	}

	@Override
	public void addArgument(int index, Expression expression) {
		throw new RuntimeException("Cannot add an argument to a variable");
	}

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Object object = context.resolveVariable(variableName);
		if (object == null)
			throw new ExpressionException("Unknown variable " + variableName,
					getLine(), getColumn());
		return object;
	}

	@Override
	public void check() throws ExpressionException {
	}

	@Override
	public Class<?> getResultType() {
		return null;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public int getColumn() {
		return column;
	}

	@Override
	public void setPosition(int line, int column) {
		this.line = line;
		this.column = column;
	}

	@Override
	public String getName() {
		return "Variable";
	}
}
