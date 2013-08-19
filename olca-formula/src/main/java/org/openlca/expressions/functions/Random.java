package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

class Random extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		return Math.random();
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public void check() throws ExpressionException {
		checkNoArguments();
	}

	@Override
	public String getName() {
		return "rand";
	}

}
