package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.process.ProcessBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessParameterMapper {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final FormulaInterpreter interpreter;
	private long nextScope = 0;

	public ProcessParameterMapper(IDatabase db) {
		this.interpreter = new FormulaInterpreter();
		var dao = new ParameterDao(db);
		for (var param : dao.getGlobalParameters()) {
			interpreter.bind(param.name, param.value);
		}
	}

	/**
	 * Maps the parameter of the SimaPro process to the given openLCA process and
	 * creates an interpreter scope for this process. The parameters of the process
	 * are bound to this scope and can be used in evaluations later.
	 */
	public long map(ProcessBlock block, Process process) {
		long scopeId = ++nextScope;
		Scope scope = interpreter.createScope(scopeId);
		for (var row : block.inputParameters()) {
			var p = Parameters.create(row, ParameterScope.PROCESS);
			process.parameters.add(p);
			scope.bind(p.name, Double.toString(p.value));
		}
		for (var row : block.calculatedParameters()) {
			var p = Parameters.create(row, ParameterScope.PROCESS);
			process.parameters.add(p);
			scope.bind(p.name, p.formula);
		}
		evalParameters(process, scope);
		return scopeId;
	}

	private void evalParameters(Process process, Scope scope) {
		for (var param : process.parameters) {
			if (param.isInputParameter)
				continue;
			try {
				param.value = scope.eval(param.name);
			} catch (Exception e) {
				log.error("failed to evaluate process parameter " + param
						+ "; set it as an input parameter with value 1", e);
				param.formula = null;
				param.value = 1;
				param.isInputParameter = true;
				scope.bind(param.name, 1);
			}
		}
	}

	public double eval(String formula, long scopeId) {
		try {
			var scope = interpreter.getScopeOrGlobal(scopeId);
			return scope.eval(formula);
		} catch (Exception e) {
			log.error("failed to evaluate formula " + formula
					+ "; set value to 0", e);
			return 0;
		}
	}
}
