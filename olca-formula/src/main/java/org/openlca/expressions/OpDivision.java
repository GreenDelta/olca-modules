package org.openlca.expressions;

class OpDivision extends AbstractNumericOperator {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Double result = (Double) arguments.get(0).evaluate(context);
		for (int i = 1; i < arguments.size(); i++) {
			Expression function = arguments.get(i);
			Double denom = (Double) function.evaluate(context);
			if (denom == 0)
				throw new ExpressionException("Cannot divide by 0");
			result = result / denom;
		}
		return result;
	}

	@Override
	public String getName() {
		return "/";
	}
}