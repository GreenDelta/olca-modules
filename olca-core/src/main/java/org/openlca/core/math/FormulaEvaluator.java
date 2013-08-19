package org.openlca.core.math;

import java.util.Collection;
import java.util.HashMap;

import org.openlca.core.model.Parameter;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;
import org.openlca.expressions.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation changed with new formula interpreter.
 */
public class FormulaEvaluator {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void evaluate(Collection<? extends Parameter> parameters)
			throws FormulaParseException {
		try {
			HashMap<String, Parameter> params = mapParameters(parameters);
			HashMap<String, Variable> variables = mapVariables(params);
			eval(variables);
			mapValues(variables, parameters);
		} catch (Exception e) {
			log.warn("Formula evaluation failed " + e.getMessage(), e);
			throw new FormulaParseException(e);
		}
	}

	private HashMap<String, Parameter> mapParameters(
			Collection<? extends Parameter> parameters)
			throws FormulaParseException {
		HashMap<String, Parameter> map = new HashMap<>();
		for (Parameter parameter : parameters) {
			mapParameter(map, parameter);
		}
		return map;
	}

	private void mapParameter(HashMap<String, Parameter> map,
			Parameter parameter) throws FormulaParseException {
		String name = parameter.getName();
		Parameter old = map.get(name);
		if (old == null)
			map.put(name, parameter);
		else {
			int oldLevel = getLevel(old);
			int newLevel = getLevel(parameter);
			if (oldLevel == newLevel)
				throw new FormulaParseException(
						"Duplicate declaration of parameter " + name);
			if (newLevel > oldLevel)
				map.put(name, parameter);
		}
	}

	private HashMap<String, Variable> mapVariables(
			HashMap<String, Parameter> params) {
		HashMap<String, Variable> map = new HashMap<>();
		for (String name : params.keySet()) {
			Parameter param = params.get(name);
			Variable variable = new Variable(name, param.getExpression()
					.getFormula());
			map.put(name, variable);
		}
		return map;
	}

	private int getLevel(Parameter param) {
		if (param == null || param.getType() == null)
			return -1;
		return param.getType().getLevel();
	}

	private void eval(HashMap<String, Variable> variables)
			throws InterpreterException {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		for (Variable var : variables.values())
			interpreter.bind(var);
		interpreter.evalVariables();
	}

	private void mapValues(HashMap<String, Variable> variables,
			Collection<? extends Parameter> parameters) {
		for (Parameter param : parameters) {
			Variable var = variables.get(param.getName());
			param.getExpression().setValue(var.getValue());
		}
	}

}
