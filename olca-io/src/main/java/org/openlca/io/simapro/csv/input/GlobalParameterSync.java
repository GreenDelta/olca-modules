package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports the project and database parameters from SimaPro as openLCA database
 * parameters.
 */
class GlobalParameterSync {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final CsvDataSet dataSet;
	private final ParameterDao dao;

	public GlobalParameterSync(CsvDataSet dataSet, IDatabase db) {
		this.dataSet = dataSet;
		dao = new ParameterDao(db);
	}

	public void run() {
		log.trace("import project and database parameters");

		List<Parameter> globals = loadGlobals();
		HashSet<String> added = new HashSet<>();

		Stream.concat(
			dataSet.databaseInputParameters().stream(),
			dataSet.projectInputParameters().stream())
			.forEach(row -> {
				if (contains(row.name(), globals))
					return;
				var param = Parameters.create(row, ParameterScope.GLOBAL);
				added.add(param.name);
				globals.add(param);
		});

		for (InputParameterRow row : index.getInputParameters()) {

		}

		for (CalculatedParameterRow row : index.getCalculatedParameters()) {
			if (contains(row.name, globals))
				continue;
			Parameter param = Parameters.create(row, ParameterScope.GLOBAL);
			globals.add(param);
			added.add(param.name);
		}
		evalAndWrite(globals, added);
	}

	private void evalAndWrite(List<Parameter> globals, HashSet<String> added) {
		FormulaInterpreter interpreter = new FormulaInterpreter();
		for (Parameter param : globals) {
			interpreter.bind(param.name, param.formula);
		}
		for (Parameter param : globals) {
			if (!added.contains(param.name))
				continue;
			if (!param.isInputParameter) {
				eval(param, interpreter);
			}
			dao.insert(param);
		}
	}

	private void eval(Parameter p, FormulaInterpreter interpreter) {
		try {
			p.value = interpreter.eval(p.formula);
		} catch (Exception e) {
			log.warn("failed to evaluate formula for global parameter "
					+ p.name + ": set value to 1.0", e);
			p.isInputParameter = true;
			p.value = 1.0;
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
			if (Strings.nullOrEqual(paramName, global.name)) {
				log.warn("a global paramater {} already exists in the "
						+ "database and thus was not imported", paramName);
				return true;
			}
		}
		return false;
	}

}
