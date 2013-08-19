// ported from the xReporter project
package org.openlca.expressions;

class DecimalConstantFunction implements Expression {

	protected Double value;

	protected String position;

	protected int line, column;

	public DecimalConstantFunction(Double value) {
		this.value = value;
	}

	@Override
	public void addArgument(Expression function) {
		throw new RuntimeException("Cannot add an argument to a constant");
	}

	@Override
	public void addArgument(int index, Expression expression) {
		throw new RuntimeException("Cannot add an argument to a constant");
	}

	@Override
	public Object evaluate(Scope context) {
		return value;
	}

	@Override
	public void check() throws ExpressionException {
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
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
		return "Constant";
	}
}
