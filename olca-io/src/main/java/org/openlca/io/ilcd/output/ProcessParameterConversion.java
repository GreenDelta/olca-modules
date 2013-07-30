package org.openlca.io.ilcd.output;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterType;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.ParameterExtension;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessParameterConversion {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Process process;
	private IDatabase database;

	public ProcessParameterConversion(Process process, IDatabase database) {
		this.process = process;
		this.database = database;
	}

	public List<org.openlca.ilcd.processes.Parameter> run() {
		log.trace("Create process parameters.");
		List<org.openlca.ilcd.processes.Parameter> params = processParams();
		try {
			addDatabaseParams(params);
		} catch (Exception e) {
			log.error("Failed to add database parameters", e);
		}
		return params;
	}

	private void addDatabaseParams(
			List<org.openlca.ilcd.processes.Parameter> params) {
		ParameterDao dao = new ParameterDao(database);
		for (Parameter param : dao.getAllForType(ParameterType.DATABASE)) {
			if (!valid(param))
				continue;
			org.openlca.ilcd.processes.Parameter iParam = convertParam(param);
			params.add(iParam);
			addScope(iParam, ParameterType.DATABASE);
		}
	}

	private List<org.openlca.ilcd.processes.Parameter> processParams() {
		List<org.openlca.ilcd.processes.Parameter> iParameters = new ArrayList<>();
		for (Parameter oParam : process.getParameters()) {
			if (!valid(oParam))
				continue;
			org.openlca.ilcd.processes.Parameter iParam = convertParam(oParam);
			iParameters.add(iParam);
			addScope(iParam, ParameterType.PROCESS);
		}
		return iParameters;
	}

	private org.openlca.ilcd.processes.Parameter convertParam(Parameter oParam) {
		org.openlca.ilcd.processes.Parameter iParameter = new org.openlca.ilcd.processes.Parameter();
		iParameter.setName(oParam.getName());
		if (oParam.getExpression() != null) {
			iParameter.setFormula(oParam.getExpression().getFormula());
			iParameter.setMeanValue(oParam.getExpression().getValue());
		}
		if (Strings.notEmpty(oParam.getDescription())) {
			iParameter.getComment().add(
					LangString.label(oParam.getDescription()));
		}
		return iParameter;
	}

	private boolean valid(Parameter param) {
		if (param == null || Strings.nullOrEmpty(param.getName()))
			return false;
		if (param.getExpression() == null)
			return false;
		return true;
	}

	private void addScope(org.openlca.ilcd.processes.Parameter param,
			ParameterType type) {
		String scope = getScope(type);
		new ParameterExtension(param).setScope(scope);
	}

	private String getScope(ParameterType type) {
		if (type == null)
			return "unspecified";
		switch (type) {
		case DATABASE:
			return "global";
		case PROCESS:
			return "process";
		case PRODUCT_SYSTEM:
			return "system";
		default:
			return "unspecified";
		}
	}
}
