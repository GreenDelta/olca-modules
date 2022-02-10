package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.util.Strings;

/**
 * Imports the project and database parameters from SimaPro as openLCA database
 * parameters.
 */
class GlobalParameterSync {

	private final IDatabase db;
	private final ImportLog log;
	private final Map<String, Parameter> globals = new HashMap<>();

	GlobalParameterSync(IDatabase db, ImportLog log) {
		this.db = db;
		this.log = log;
		new ParameterDao(db).getGlobalParameters()
			.forEach(param -> globals.put(keyOf(param.name), param));
	}

	void sync(CsvDataSet dataSet) {
		log.info("sync project and database parameters");

		// global input parameters
		Stream.concat(
				dataSet.databaseInputParameters().stream(),
				dataSet.projectInputParameters().stream())
			.forEach(row -> {
				var key = keyOf(row.name());
				if (globals.containsKey(key))
					return;
				var param = Parameters.create(row, ParameterScope.GLOBAL);
				param = db.insert(param);
				log.imported(param);
				globals.put(key, param);
			});

		// global calculated parameters
		var newCalculated = new ArrayList<Parameter>();
		Stream.concat(
				dataSet.databaseCalculatedParameters().stream(),
				dataSet.projectCalculatedParameters().stream())
			.forEach(row -> {
				var key = keyOf(row.name());
				if (globals.containsKey(key))
					return;
				var param = Parameters.create(
					dataSet, row, ParameterScope.GLOBAL);
				newCalculated.add(param);
				globals.put(key, param);
			});

		evalAndWrite(newCalculated);
	}

	private void evalAndWrite(List<Parameter> newCalculated) {

		// create the interpreter
		var interpreter = new FormulaInterpreter();
		for (var param : globals.values()) {
			if (param.isInputParameter) {
				interpreter.bind(param.name, param.value);
			} else {
				interpreter.bind(param.name, param.formula);
			}
		}

		// evaluate and insert the parameters
		for (var param : newCalculated) {
			try {
				param.value = interpreter.eval(param.formula);
			} catch (Exception e) {
				log.warn("failed to evaluate formula for global parameter "
					+ param.name + ": " + param.formula);
				param.isInputParameter = true;
				param.value = 1.0;
			}
			param = db.insert(param);
			log.imported(param);
		}
	}

	private String keyOf(String name) {
		return Strings.nullOrEmpty(name)
			? null
			: name.trim().toLowerCase();
	}

}
