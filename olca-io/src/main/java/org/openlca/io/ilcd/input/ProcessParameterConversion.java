package org.openlca.io.ilcd.input;

import java.util.UUID;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.ParameterExtension;
import org.openlca.ilcd.util.Processes;
import org.openlca.io.ilcd.Ext;
import org.openlca.commons.Strings;

/**
 * Adds the parameters of an ILCD process data set to an openLCA process data
 * set. If the scope of the parameter is 'global' it is inserted as database
 * parameter if it not yet exists.
 */
class ProcessParameterConversion {

	private final Process olcaProcess;
	private final ParameterDao dao;
	private final Import imp;

	public ProcessParameterConversion(Process olcaProcess, Import imp) {
		this.olcaProcess = olcaProcess;
		this.imp = imp;
		this.dao = new ParameterDao(imp.db());
	}

	public void run(org.openlca.ilcd.processes.Process ds) {
		for (var p : Processes.getParameters(ds)) {
			if (p.getName() == null || p.getName().startsWith("temp_olca_param"))
				continue;
			var param = convertParameter(p);
			addOrInsert(param);
		}
	}

	private Parameter convertParameter(
			org.openlca.ilcd.processes.Parameter iParameter) {
		var scope = isGlobal(iParameter)
				? ParameterScope.GLOBAL
				: ParameterScope.PROCESS;
		var param = new Parameter();
		param.refId = Ext.getUUID(iParameter).orElse(UUID.randomUUID().toString());
		param.scope = scope;
		param.name = iParameter.getName();
		param.description = imp.str(iParameter.getComment());
		Double mean = iParameter.getMean();
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
		String formula = iParameter.getFormula();
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
		var ext = new ParameterExtension(iParameter);
		var scope = ext.getScope();
		return "global".equals(scope);
	}

	private void addOrInsert(Parameter param) {
		if (param.scope == ParameterScope.PROCESS) {
			olcaProcess.parameters.add(param);
			return;
		}
		var globals = dao.getGlobalParameters();
		for (var global : globals) {
			if (Strings.equalsIgnoreCase(param.name, global.name))
				return;
		}
		dao.insert(param);
	}
}
