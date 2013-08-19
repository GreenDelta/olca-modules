// ported from the xReporter project
package org.openlca.expressions;

class OpGreaterThan extends AbstractComparisonExpression {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object evaluate(Scope context) throws ExpressionException {
		Comparable value1 = (Comparable) arguments.get(0).evaluate(context);
		Comparable value2 = (Comparable) arguments.get(1).evaluate(context);
		if (value1.compareTo(value2) > 0)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}

	@Override
	public String getName() {
		return ">";
	}
}