// ported from the xReporter project
package org.openlca.expressions;

public abstract class AbstractNumericOperator extends AbstractExpression {

	@Override
	public void check() throws ExpressionException {
		if (arguments.size() != 2)
			throw new ExpressionException(
					"Numeric operator requires 2 arguments", getLine(),
					getColumn());

		Expression expr1 = arguments.get(0);
		if (expr1.getResultType() != null
				&& !Double.class.isAssignableFrom(expr1.getResultType()))
			throw new ExpressionException(
					"Non-decimal argument for numeric operator",
					expr1.getLine(), expr1.getColumn());

		Expression expr2 = arguments.get(1);
		if (expr2.getResultType() != null
				&& !Double.class.isAssignableFrom(expr2.getResultType()))
			throw new ExpressionException(
					"Non-decimal argument for numeric operator",
					expr2.getLine(), expr2.getColumn());
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}
}
