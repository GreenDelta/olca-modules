package org.openlca.io.ilcd.input;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Expression;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterType;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.ParameterExtension;
import org.openlca.ilcd.util.ProcessBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the parameters of an ILCD process data set to an openLCA process data
 * set. If the scope of the parameter is 'global' it is inserted as database
 * parameter if it not yet exists.
 */
class ProcessParameterConversion {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Process olcaProcess;
	private ParameterDao dao;

	public ProcessParameterConversion(Process olcaProcess, IDatabase database) {
		this.olcaProcess = olcaProcess;
		this.dao = new ParameterDao(database);
	}

	public void run(ProcessBag ilcdProcess) {
		List<org.openlca.ilcd.processes.Parameter> iParameters = ilcdProcess
				.getParameters();
		for (org.openlca.ilcd.processes.Parameter iParameter : iParameters) {
			if (iParameter.getName() == null
					|| iParameter.getName().startsWith("temp_olca_param"))
				continue;
			Parameter param = convertParameter(iParameter);
			addOrInsert(param);
		}
	}

	private Parameter convertParameter(
			org.openlca.ilcd.processes.Parameter iParameter) {
		Expression expression = createParameterExpression(iParameter);
		ParameterType type = ParameterType.PROCESS;
		if (isGlobal(iParameter))
			type = ParameterType.DATABASE;
		Parameter param = new Parameter();
		param.setName(iParameter.getName());
		param.setDescription(LangString.getLabel(iParameter.getComment()));
		param.setType(type);
		param.getExpression().setValue(expression.getValue());
		param.getExpression().setFormula(expression.getFormula());
		return param;
	}

	private Expression createParameterExpression(
			org.openlca.ilcd.processes.Parameter iParameter) {
		Expression expression = new Expression();
		Double mean = iParameter.getMeanValue();
		if (mean != null) {
			expression.setValue(mean);
		}
		if (iParameter.getFormula() != null) {
			expression.setFormula(iParameter.getFormula());
		} else if (mean != null) {
			expression.setFormula(mean.toString());
		}
		return expression;
	}

	private boolean isGlobal(org.openlca.ilcd.processes.Parameter iParameter) {
		ParameterExtension ext = new ParameterExtension(iParameter);
		String scope = ext.getScope();
		if (scope == null)
			return false;
		return "global".equals(scope);
	}

	private void addOrInsert(Parameter param) {
		if (param.getType() == ParameterType.PROCESS) {
			olcaProcess.getParameters().add(param);
			return;
		}
		try {
			List<Parameter> params = dao.getAllForName(param.getName(),
					param.getType());
			if (params.isEmpty())
				dao.insert(param);
		} catch (Exception e) {
			log.error("Failed to store parameter in database", e);
		}
	}

}
