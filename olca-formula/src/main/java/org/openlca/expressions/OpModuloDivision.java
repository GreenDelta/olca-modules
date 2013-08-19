package org.openlca.expressions;

class OpModuloDivision extends AbstractNumericOperator {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Double result = (Double) arguments.get(0).evaluate(context);
		for (int i = 1; i < arguments.size(); i++) {
			Expression function = arguments.get(i);
			result = result % (Double) function.evaluate(context);
		}
		return result;
	}

	@Override
	public String getName() {
		return "mod";
	}

}
