package org.openlca.expressions;

class OpXor extends AbstractExpression {

	@Override
	public Object evaluate(Scope context) throws ExpressionException {
		Boolean b1 = (Boolean) arguments.get(0).evaluate(context);
		Boolean b2 = (Boolean) arguments.get(1).evaluate(context);
		return b1.booleanValue() != b2.booleanValue();
	}

	@Override
	public Class<?> getResultType() {
		return Boolean.class;
	}

	@Override
	public String getName() {
		return "xor";
	}

	@Override
	public void check() throws ExpressionException {
		checkArgumentsOfSameType(Boolean.class);
		if (arguments.size() != 2) {
			throw new ExpressionException("XOR function takes 2 arguments.",
					line, column);
		}
	}

}
