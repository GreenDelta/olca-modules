// ported from the xReporter project
package org.openlca.expressions;

import java.util.ArrayList;

public abstract class AbstractExpression implements Expression {

	protected ArrayList<Expression> arguments = new ArrayList<>(3);

	protected int line, column;

	@Override
	public void addArgument(Expression expression) {
		arguments.add(expression);
	}

	@Override
	public void addArgument(int index, Expression expression) {
		arguments.add(index, expression);
	}

	@Override
	public void setPosition(int line, int column) {
		this.line = line;
		this.column = column;
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
	public void check() throws ExpressionException {
	}

	protected void checkArguments(Class<?>[] types) throws ExpressionException {
		if (arguments.size() != types.length)
			throw new ExpressionException(getName() + " requires "
					+ types.length + " argument(s)", getLine(), getColumn());

		for (int i = 0; i < types.length; i++)
			checkArgument(i, types[i]);
	}

	protected void checkArgument(int position, Class<?> clazz)
			throws ExpressionException {
		Class<?> resultClass = arguments.get(position).getResultType();
		if (resultClass != null && !clazz.isAssignableFrom(resultClass))
			throw new ExpressionException("Argument " + position + " of "
					+ getName() + " is of an incorrect type", getLine(),
					getColumn());
	}

	protected void checkNoArguments() throws ExpressionException {
		if (arguments.size() > 0)
			throw new ExpressionException(getName() + " takes no parameters",
					getLine(), getColumn());
	}

	protected void checkArgumentsOfSameType(Class<?> clazz)
			throws ExpressionException {
		for (int i = 0; i < arguments.size(); i++) {
			Expression expression = arguments.get(i);
			if (expression.getResultType() != null
					&& !clazz.isAssignableFrom(expression.getResultType()))
				throw new ExpressionException("Argument " + i + " of "
						+ getName() + " is of an incorrect type", getLine(),
						getColumn());
		}
	}

}
