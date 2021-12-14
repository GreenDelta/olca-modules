package org.openlca.io.ilcd.input;

import java.util.List;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
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

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Process olcaProcess;
	private final ParameterDao dao;
	private final ImportConfig config;

	public ProcessParameterConversion(Process olcaProcess, ImportConfig config) {
		this.olcaProcess = olcaProcess;
		this.config = config;
		this.dao = new ParameterDao(config.db());
	}

	public void run(ProcessBag ilcdProcess) {
		List<org.openlca.ilcd.processes.Parameter> iParameters = ilcdProcess
				.getParameters();
		for (org.openlca.ilcd.processes.Parameter iParameter : iParameters) {
			if (iParameter.name == null
					|| iParameter.name.startsWith("temp_olca_param"))
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
		param.scope = scope;
		param.name = iParameter.name;
		param.description = config.str(iParameter.comment);
		Double mean = iParameter.mean;
		if (mean != null)
			param.value = mean;
		new UncertaintyConverter().map(iParameter, param);
		param.isInputParameter = true;
		String formula = getFormula(iParameter);
		if (formula != null && scope == ParameterScope.PROCESS) {
			param.formula = formula;
			param.isInputParameter = false;
		}
		return param;
	}

	private String getFormula(org.openlca.ilcd.processes.Parameter iParameter) {
		String formula = iParameter.formula;
		if (formula == null)
			return null;
		try {
			Double.parseDouble(formula);
			return null; // the "formula" is a plain number
		} catch (Exception e) {
			// in openLCA the parameter separator of a function is always a
			// semicolon and the decimal separator always a dot
			// some databases use a comma as a separator for function parameters
			// which we replace here by a semicolon
			return formula.replace(',', ';');
		}
	}

	private boolean isGlobal(org.openlca.ilcd.processes.Parameter iParameter) {
		ParameterExtension ext = new ParameterExtension(iParameter);
		String scope = ext.getScope();
		if (scope == null)
			return false;
		return "global".equals(scope);
	}

	private void addOrInsert(Parameter param) {
		if (param.scope == ParameterScope.PROCESS) {
			olcaProcess.parameters.add(param);
			return;
		}
		try {
			List<Parameter> params = dao.getGlobalParameters();
			boolean contains = false;
			for (Parameter dbParam : params) {
				if (Strings.nullOrEqual(param.name, dbParam.name)) {
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
