// ported from the xReporter project
package org.openlca.expressions;

import java.util.Iterator;

class OpOr extends AbstractExpression {
	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		boolean result = false;
		Iterator<?> argIt = arguments.iterator();
		while (argIt.hasNext()) {
			result = result
					|| ((Boolean) ((Expression) argIt.next()).evaluate(context))
							.booleanValue();
			if (result)
				return Boolean.TRUE;
		}
		if (result)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}

	@Override
	public void check() throws ExpressionException {
		checkArgumentsOfSameType(Boolean.class);
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}

	@Override
	public String getName() {
		return "||";
	}
}