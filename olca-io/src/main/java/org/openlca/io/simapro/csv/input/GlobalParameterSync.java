package org.openlca.io.simapro.csv.input;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Imports the project and database parameters from SimaPro as openLCA database
 * parameters. We (currently) do not support calculated parameters at the
 * database level. Thus, we try to evaluate the calculated parameters from
 * SimaPro and store them as input parameters during this import.
 */
class GlobalParameterSync {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final SpRefDataIndex index;
	private final ParameterDao dao;

	public GlobalParameterSync(SpRefDataIndex index, IDatabase database) {
		this.index = index;
		dao = new ParameterDao(database);
	}

	public void run() {
		log.trace("import project and database parameters");
		List<Parameter> globals = loadGlobals();
		for (InputParameterRow row : index.getInputParameters()) {
			if (contains(row.getName(), globals))
				continue;
			Parameter param = create(row);
			globals.add(param);
		}
		FormulaInterpreter interpreter = createInterpreter(globals);
		for (CalculatedParameterRow row : index.getCalculatedParameters()) {
			if (contains(row.getName(), globals))
				continue;
			Parameter param = create(row, interpreter);
			globals.add(param);
		}
	}

	private FormulaInterpreter createInterpreter(List<Parameter> globals) {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		for (Parameter global : globals) {
			String name = global.getName();
			String val = Double.toString(global.getValue());
			interpreter.bind(name, val);
		}
		return interpreter;
	}

	private Parameter create(InputParameterRow row) {
		Parameter param = new Parameter();
		param.setName(row.getName());
		param.setInputParameter(true);
		param.setScope(ParameterScope.GLOBAL);
		param.setValue(row.getValue());
		dao.insert(param);
		return param;
	}

	private Parameter create(CalculatedParameterRow row,
			FormulaInterpreter interpreter) {
		Parameter param = new Parameter();
		param.setName(row.getName());
		param.setInputParameter(true);
		param.setScope(ParameterScope.GLOBAL);
		try {
			double val = interpreter.eval(row.getExpression());
			param.setValue(val);
		} catch (Exception e) {
			log.error(
					"failed to evaluate formula for global parameter "
							+ row.getName() + " set value to 1.0", e);
			param.setValue(1.0);
		}
		dao.insert(param);
		return param;
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