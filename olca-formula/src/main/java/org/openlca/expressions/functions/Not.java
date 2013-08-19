package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

class Not extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		if (arguments == null || arguments.size() == 0)
			return false;

		boolean result = ((Boolean) arguments.get(0).evaluate(context))
				.booleanValue();
		if (result)
			return Boolean.FALSE;
		return Boolean.TRUE;
	}

	@Override
	public void check() throws ExpressionException {
		if (arguments != null && arguments.size() > 0)
			checkArguments(new Class[] { Boolean.class });
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}

	@Override
	public String getName() {
		return "not";
	}

}
