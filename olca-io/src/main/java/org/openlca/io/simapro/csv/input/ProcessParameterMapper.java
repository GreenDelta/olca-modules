package org.openlca.io.simapro.csv.input;

import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.process.ProcessBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

class ProcessParameterMapper {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final ParameterDao dao;
	private final FormulaInterpreter interpreter;
	private Process process;

	private long nextScope = 0;

	public ProcessParameterMapper(IDatabase database) {
		this.dao = new ParameterDao(database);
		this.interpreter = initInterpreter(dao);
	}

	private FormulaInterpreter initInterpreter(ParameterDao dao) {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		for (Parameter parameter : dao.getGlobalParameters()) {
			interpreter.bind(parameter.getName(),
					Double.toString(parameter.getValue()));
		}
		return interpreter;
	}

	/**
	 * Maps the parameter of the SimaPro process to the given openLCA process
	 * and creates an interpreter scope for this process. The parameters of the
	 * process are bound to this scope and can be used in evaluations later.
	 */
	public long map(ProcessBlock block, Process process) {
		this.process = process;
		long scopeId = ++nextScope;
		Scope scope = interpreter.createScope(scopeId);
		for (InputParameterRow row : block.getInputParameters()) {
			Parameter parameter = create(row);
			String val = Double.toString(parameter.getValue());
			scope.bind(parameter.getName(), val);
		}
		for (CalculatedParameterRow row : block.getCalculatedParameters()) {
			Parameter parameter = create(row);
			scope.bind(parameter.getName(), parameter.getFormula());
		}
		evalProcessParameters(scopeId);
		this.process = null;
		return scopeId;
	}

	private Parameter create(InputParameterRow row) {
		Parameter p = new Parameter();
		p.setRefId(UUID.randomUUID().toString());
		p.setName(row.getName());
		p.setInputParameter(true);
		p.setScope(ParameterScope.PROCESS);
		p.setValue(row.getValue());
		p.setUncertainty(Uncertainties.get(row.getValue(),
				row.getUncertainty()));
		p.setDescription(row.getComment());
		process.getParameters().add(p);
		return p;
	}

	private Parameter create(CalculatedParameterRow row) {
		Parameter p = new Parameter();
		p.setRefId(UUID.randomUUID().toString());
		p.setName(row.getName());
		p.setInputParameter(false);
		p.setScope(ParameterScope.PROCESS);
		String expr = row.getExpression();
		if (!Strings.isNullOrEmpty(expr))
			expr = expr.replace(',', '.');
		p.setFormula(expr);
		p.setDescription(row.getComment());
		process.getParameters().add(p);
		return p;
	}

	private void evalProcessParameters(long scopeId) {
		Scope scope = interpreter.getScope(scopeId);
		for (Parameter param : process.getParameters()) {
			if (param.isInputParameter())
				continue;
			try {
				double val = scope.eval(param.getName());
				param.setValue(val);
			} catch (Exception e) {
				log.error("failed to evaluate process parameter " + param
						+ "; set it as an input parameter with value 1", e);
				param.setFormula(null);
				param.setValue(1);
				param.setInputParameter(true);
				scope.bind(param.getName(), "1");
			}
		}
	}

	public double eval(String formula, long scopeId) {
		try {
			Scope scope = interpreter.getScope(scopeId);
			return scope.eval(formula);
		} catch (Exception e) {
			log.error("failed to evaluate formula " + formula
					+ "; set value to NaN", e);
			return Double.NaN;
		}
	}
}
