package org.openlca.expressions.functions;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

public class If extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		boolean result = ((Boolean) arguments.get(0).evaluate(context))
				.booleanValue();
		if (result)
			return arguments.get(1).evaluate(context);
		return arguments.get(2).evaluate(context);
	}

	@Override
	public void check() throws ExpressionException {
		checkArguments(new Class<?>[] { Boolean.class, Double.class,
				Double.class });
	}

	@Override
	public Class<?> getResultType() {
		return Double.class;
	}

	@Override
	public String getName() {
		return "if";
	}
}
