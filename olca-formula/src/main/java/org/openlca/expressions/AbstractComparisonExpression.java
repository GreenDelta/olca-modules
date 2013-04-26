// ported from the xReporter project
package org.openlca.expressions;

abstract class AbstractComparisonExpression extends AbstractExpression {

	@Override
	public void check() throws ExpressionException {
		if (arguments.size() != 2)
			throw new ExpressionException(
					"Comparison operator should have exactly 2 arguments",
					getLine(), getColumn());

		Expression functionA = arguments.get(0);
		if (functionA.getResultType() != null
				&& !Comparable.class
						.isAssignableFrom(functionA.getResultType()))
			throw new ExpressionException(
					"Invalid argument for comparison expression", getLine(),
					getColumn());

		Expression functionB = arguments.get(1);
		if (functionA.getResultType() != null
				&& functionB.getResultType() != null
				&& !(functionA.getResultType() == functionB.getResultType()))
			throw new ExpressionException(
					"Cannot compare things of different types", getLine(),
					getColumn());
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}
}
