package org.openlca.expressions;

class OpIntegerDivision extends AbstractNumericOperator {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Double result = (Double) arguments.get(0).evaluate(context);
		for (int i = 1; i < arguments.size(); i++) {
			Expression function = arguments.get(i);
			Double value = (Double) function.evaluate(context);
			result = (double) (Math.round(result) / Math.round(value));
		}
		return result;
	}

	@Override
	public String getName() {
		return "div";
	}
}
