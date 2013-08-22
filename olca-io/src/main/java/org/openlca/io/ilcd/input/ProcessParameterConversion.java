package org.openlca.io.ilcd.input;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.ParameterExtension;
import org.openlca.ilcd.util.ProcessBag;
import org.openlca.util.Strings;
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
		ParameterScope scope = ParameterScope.PROCESS;
		if (isGlobal(iParameter))
			scope = ParameterScope.GLOBAL;
		Parameter param = new Parameter();
		param.setScope(scope);
		param.setName(iParameter.getName());
		Double mean = iParameter.getMeanValue();
		param.setDescription(LangString.getLabel(iParameter.getComment()));
		if (mean != null)
			param.setValue(mean);
		param.setInputParameter(true);
		if (iParameter.getFormula() != null && scope == ParameterScope.PROCESS) {
			param.setFormula(iParameter.getFormula());
			param.setInputParameter(false);
		}
		return param;
	}

	private boolean isGlobal(org.openlca.ilcd.processes.Parameter iParameter) {
		ParameterExtension ext = new ParameterExtension(iParameter);
		String scope = ext.getScope();
		if (scope == null)
			return false;
		return "global".equals(scope);
	}

	private void addOrInsert(Parameter param) {
		if (param.getScope() == ParameterScope.PROCESS) {
			olcaProcess.getParameters().add(param);
			return;
		}
		try {
			List<Parameter> params = dao.getGlobalParameters();
			boolean contains = false;
			for (Parameter dbParam : params) {
				if (Strings.nullOrEqual(param.getName(), dbParam.getName())) {
					contains = true;
					break;
				}
			}
			if (!contains)
				dao.insert(param);
		} catch (Exception e) {
			log.error("Failed to store parameter in database", e);
		}
	}

}
