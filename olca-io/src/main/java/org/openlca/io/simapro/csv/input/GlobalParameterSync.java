package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
		Parameter p = new Parameter();
		p.setRefId(UUID.randomUUID().toString());
		p.setName(row.getName());
		p.setInputParameter(true);
		p.setScope(ParameterScope.GLOBAL);
		p.setValue(row.getValue());
		p.setDescription(row.getComment());
		p.setUncertainty(Uncertainties.get(row.getValue(),
				row.getUncertainty()));
		dao.insert(p);
		return p;
	}

	private Parameter create(CalculatedParameterRow row,
			FormulaInterpreter interpreter) {
		Parameter p = new Parameter();
		p.setRefId(UUID.randomUUID().toString());
		p.setName(row.getName());
		p.setScope(ParameterScope.GLOBAL);
		p.setDescription(row.getComment());
		p.setInputParameter(false);
		try {
			String expr = row.getExpression();
			double val = interpreter.eval(expr);
			p.setValue(val);
			p.setFormula(expr);
		} catch (Exception e) {
			log.error("failed to evaluate formula for global parameter "
					+ row.getName() + ": set value to 1.0", e);
			p.setInputParameter(true);
			p.setValue(1.0);
		}
		dao.insert(p);
		return p;
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