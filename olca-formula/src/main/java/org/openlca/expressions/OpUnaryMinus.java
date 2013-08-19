package org.openlca.expressions;

class OpUnaryMinus extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		return -((Double) arguments.get(0).evaluate(context));
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public void check() throws ExpressionException {
		if (arguments.size() != 1)
			throw new ExpressionException("Negation requires one argument",
					getLine(), getColumn());

		Class<?> resultType = arguments.get(0).getResultType();
		if (resultType != null && resultType != Double.class)
			throw new ExpressionException(
					"Negation can only be applied to decimals", getLine(),
					getColumn());
	}

	@Override
	public String getName() {
		return "-";
	}
}