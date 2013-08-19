package org.openlca.expressions;

public interface Expression {

	public void addArgument(Expression expression);

	public void addArgument(int index, Expression expression);

	public Object evaluate(Scope context) throws ExpressionException;

	public void check() throws ExpressionException;

	public Class<?> getResultType();

	public int getLine();

	public int getColumn();

	public void setPosition(int line, int column);

	public String getName();
}
