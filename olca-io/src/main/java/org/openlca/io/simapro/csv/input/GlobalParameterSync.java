package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports the project and database parameters from SimaPro as openLCA database
 * parameters.
 */
class GlobalParameterSync {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final SpRefDataIndex index;
	private final ParameterDao dao;

	public GlobalParameterSync(SpRefDataIndex index, IDatabase db) {
		this.index = index;
		dao = new ParameterDao(db);
	}

	public void run() {
		log.trace("import project and database parameters");
		List<Parameter> globals = loadGlobals();
		HashSet<String> added = new HashSet<>();
		for (InputParameterRow row : index.getInputParameters()) {
			if (contains(row.getName(), globals))
				continue;
			Parameter param = Parameters.create(row, ParameterScope.GLOBAL);
			added.add(param.getName());
			globals.add(param);
		}
		for (CalculatedParameterRow row : index.getCalculatedParameters()) {
			if (contains(row.getName(), globals))
				continue;
			Parameter param = Parameters.create(row, ParameterScope.GLOBAL);
			globals.add(param);
			added.add(param.getName());
		}
		evalAndWrite(globals, added);
	}

	private void evalAndWrite(List<Parameter> globals, HashSet<String> added) {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		for (Parameter param : globals) {
			interpreter.bind(param.getName(), param.getFormula());
		}
		for (Parameter param : globals) {
			if (!added.contains(param.getName()))
				continue;
			if (!param.isInputParameter()) {
				eval(param, interpreter);
			}
			dao.insert(param);
		}
	}

	private void eval(Parameter p, FormulaInterpreter interpreter) {
		try {
			double val = interpreter.eval(p.getFormula());
			p.setValue(val);
		} catch (Exception e) {
			log.warn("failed to evaluate formula for global parameter "
					+ p.getName() + ": set value to 1.0", e);
			p.setInputParameter(true);
			p.setValue(1.0);
		}
	}

	private List<Parameter> loadGlobals() {
		List<Parameter> globals = new ArrayList<>();
		try {
			List<Parameter> fromDb = dao.getGlobalParameters();
			globals.addAll(fromDb);
		} catch (Exception e) {
			log.error("failed to load global parameters from database");
		}
		return globals;
	}

	private boolean contains(String paramName, List<Parameter> globals) {
		for (Parameter global : globals) {
			if (Strings.nullOrEqual(paramName, global.getName())) {
				log.warn("a global paramater {} already exists in the "
						+ "database and thus was not imported", paramName);
				return true;
			}
		}
		return false;
	}

}