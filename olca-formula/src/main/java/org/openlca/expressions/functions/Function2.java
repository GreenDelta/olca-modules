package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionContext;
import org.openlca.expressions.ExpressionException;

abstract class Function2 extends AbstractExpression {

	protected abstract double eval(double arg1, double arg2);

	@Override
	public Object evaluate(ExpressionContext context)
			throws ExpressionException {
		double arg1 = (Double) arguments.get(0)
				.evaluate(context);
		double arg2 = (Double) arguments.get(1)
				.evaluate(context);
		return eval(arg1, arg2);
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public void check() throws ExpressionException {
		checkArguments(new Class<?>[] { Double.class, Double.class });
	}

}
