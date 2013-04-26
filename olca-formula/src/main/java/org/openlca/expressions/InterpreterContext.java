package org.openlca.expressions;

import java.util.HashMap;

class InterpreterContext implements ExpressionContext {

	private HashMap<String, Integer> evaluationCalls = new HashMap<>();
	private Interpreter interpreter;
	private HashMap<String, Variable> variables;

	public InterpreterContext(Interpreter interpreter,
			HashMap<String, Variable> variables) {
		this.interpreter = interpreter;
		this.variables = variables;
	}

	void reset() {
		for (String var : evaluationCalls.keySet())
			evaluationCalls.put(var, 0);
		for (Variable variable : variables.values())
			variable.setValue(null);
	}

	@Override
	public Object get(String name) throws ExpressionException {
		return resolveVariable(name);
	}

	@Override
	public Object resolveVariable(String name) throws ExpressionException {
		Variable var = variables.get(name);
		if (var == null)
			return Constants.get(name);
		if (var.isEvaluated())
			return var.getValue();
		return callEvalutaion(var);
	}

	private Object callEvalutaion(Variable var) throws ExpressionException {
		checkCall(var);
		evaluationCalls.put(var.getName(), 1);
		try {
			interpreter.eval(var);
			return var.getValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void checkCall(Variable var) throws ExpressionException {
		Integer call = evaluationCalls.get(var.getName());
		if (call != null && call > 0)
			throw new ExpressionException("Second evaluation call on variable "
					+ var.getName() + ". Cyclic dependencies?");
	}
}