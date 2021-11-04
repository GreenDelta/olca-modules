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

		// global input parameters
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

		// global calculated parameters
		Stream.concat(
			dataSet.databaseCalculatedParameters().stream(),
			dataSet.projectCalculatedParameters().stream())
			.forEach(row -> {
				if (contains(row.name(), globals))
					return;
				var param = Parameters.create(row, ParameterScope.GLOBAL);
				globals.add(param);
				added.add(param.name);
			});

		evalAndWrite(globals, added);
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

	private void evalAndWrite(List<Parameter> globals, HashSet<String> added) {

		// create the interpreter
		var interpreter = new FormulaInterpreter();
		for (var param : globals) {
			if (param.isInputParameter) {
				interpreter.bind(param.name, param.value);
			} else {
				interpreter.bind(param.name, param.formula);
			}
		}

		// evaluate and insert the parameters
		for (var param : globals) {
			if (!added.contains(param.name))
				continue;
			if (!param.isInputParameter) {
				try {
					param.value = interpreter.eval(param.formula);
				} catch (Exception e) {
					log.warn("failed to evaluate formula for global parameter "
						+ param.name + ": set value to 1.0", e);
					param.isInputParameter = true;
					param.value = 1.0;
				}
			}
			dao.insert(param);
		}
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
