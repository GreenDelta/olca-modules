package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

class False extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		return Boolean.FALSE;
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}

	@Override
	public String getName() {
		return "false()";
	}

}
