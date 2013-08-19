package org.openlca.expressions.functions;

import java.util.Iterator;

import org.openlca.expressions.AbstractExpression;
import org.openlca.expressions.Expression;
import org.openlca.expressions.ExpressionException;
import org.openlca.expressions.Scope;

public class Or extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		boolean result = false;
		Iterator<Expression> it = arguments.iterator();
		while (!result && it.hasNext()) {
			Boolean next = (Boolean) it.next().evaluate(context);
			result = result | next;
		}
		return result;
	}

	@Override
	public void check() throws ExpressionException {
		checkArgument(0, Boolean.class);
		checkArgumentsOfSameType(Boolean.class);
	}

	@Override
	public String getName() {
		return "or";
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}

}
