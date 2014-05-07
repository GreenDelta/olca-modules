package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

class E extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		return Math.E;
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public String getName() {
		return "e()";
	}

}
