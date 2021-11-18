package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.Numeric;
import org.slf4j.LoggerFactory;

class ProcessParameters {

	/**
	 * Creates the respective process parameters and returns the evaluation
	 * scope for the process.
	 */
	static Scope map(ProcessMapper mapper) {

		// create the interpreter and bind the global parameters
		var interpreter = new FormulaInterpreter();
		var dao = new ParameterDao(mapper.context().db());
		for (var param : dao.getGlobalParameters()) {
			if (param.isInputParameter) {
				interpreter.bind(param.name, param.value);
			} else {
				interpreter.bind(param.name, param.formula);
			}
		}

		// create the evaluation scope for the process and the
		// process parameters
		var scope = interpreter.createScope(1);
		for (var row : mapper.inputParameterRows()) {
			var p = Parameters.create(row, ParameterScope.PROCESS);
			mapper.process().parameters.add(p);
			scope.bind(p.name, p.value);
		}
		for (var row : mapper.calculatedParameterRows()) {
			var p = Parameters.create(row, ParameterScope.PROCESS);
			mapper.process().parameters.add(p);
			scope.bind(p.name, p.formula);
		}

		// evaluate the calculated parameters of the process
		for (var param : mapper.process().parameters) {
			if (param.isInputParameter)
				continue;
			try {
				param.value = scope.eval(param.name);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(ProcessParameters.class);
				log.error("failed to evaluate process parameter " + param
					+ "; set it as an input parameter with value 1", e);
				param.formula = null;
				param.value = 1;
				param.isInputParameter = true;
				scope.bind(param.name, 1);
			}
		}

		return scope;
	}

	static double eval(Scope scope, Numeric numeric) {
		if (numeric == null)
			return 0;
		if (!numeric.hasFormula())
			return numeric.value();
		try {
			return scope.eval(numeric.formula());
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(ProcessParameters.class);
			log.error("failed to evaluate formula " + numeric.formula()
				+ "; set value to " + numeric.value(), e);
			return numeric.value();
		}
	}

}
