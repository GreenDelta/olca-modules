package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

abstract class FunctionN extends AbstractExpression {

	/**
	 * Returns the default value if there are no arguments given.
	 */
	protected abstract double getDefault();

	protected abstract double eval(double[] args);

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		if (arguments == null || arguments.size() == 0)
			return getDefault();
		double[] args = new double[arguments.size()];
		for (int i = 0; i < args.length; i++) {
			double arg = (Double) (arguments.get(i).evaluate(context));
			args[i] = arg;
		}
		return eval(args);
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public void check() throws ExpressionException {
		checkArgumentsOfSameType(Double.class);
	}

}
