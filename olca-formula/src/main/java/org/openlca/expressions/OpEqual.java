// ported from the xReporter project
package org.openlca.expressions;

class OpEqual extends AbstractComparisonExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Object value1 = arguments.get(0).evaluate(context);
		Object value2 = arguments.get(1).evaluate(context);
		if (value1.equals(value2))
			return Boolean.TRUE;
		return Boolean.FALSE;
	}

	@Override
	public String getName() {
		return "==";
	}
}