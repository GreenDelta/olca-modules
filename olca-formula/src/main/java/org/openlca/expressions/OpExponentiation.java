package org.openlca.expressions;

public class OpExponentiation extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Double result1 = (Double) arguments.get(0).evaluate(context);
		Double result2 = (Double) arguments.get(1).evaluate(context);
		return Math.pow(result1, result2);
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public String getName() {
		return "^";
	}

	@Override
	public void check() throws ExpressionException {
		checkArguments(new Class[] { Double.class, Double.class });
	}
}
