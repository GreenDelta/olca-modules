package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionContext;
import org.openlca.expressions.ExpressionException;

abstract class Function1 extends AbstractExpression {

	protected abstract double eval(double arg);

	@Override
	public Object evaluate(ExpressionContext context)
			throws ExpressionException {
		double arg = (Double) arguments.get(0).evaluate(context);
		return eval(arg);
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public void check() throws ExpressionException {
		checkArguments(new Class<?>[] { Double.class });
	}

}
