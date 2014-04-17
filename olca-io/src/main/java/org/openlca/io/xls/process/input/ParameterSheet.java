package org.openlca.io.xls.process.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

class ParameterSheet {

	/**
	 * The maximum number of rows for searching for a parameter section in this
	 * sheet.
	 */
	private final int MAX_ROWS = 5000;

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final Sheet sheet;

	private ParameterSheet(Config config) {
		this.config = config;
		sheet = config.workbook.getSheet("Parameters");
	}

	public static void read(Config config) {
		new ParameterSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read parameters");
			List<Parameter> globalParams = readParams("Global parameters",
					ParameterScope.GLOBAL, true);
			syncGlobals(globalParams);
			List<Parameter> params = config.process.getParameters();
			params.addAll(readParams("Input parameters",
					ParameterScope.PROCESS, true));
			params.addAll(readParams("Dependent parameters",
					ParameterScope.PROCESS, false));
		} catch (Exception e) {
			log.error("failed to read parameter sheet", e);
		}
	}

	private void syncGlobals(List<Parameter> sheetParams) {
		ParameterDao dao = new ParameterDao(config.database);
		List<Parameter> globals = new ArrayList<>();
		globals.addAll(dao.getGlobalParameters());
		for (Parameter sheetParam : sheetParams) {
			boolean found = false;
			for (Parameter global : globals) {
				if (global.getName() == null) {
					continue;
				}
				if (global.getName().equalsIgnoreCase(sheetParam.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				globals.add(dao.insert(sheetParam));
			}
		}
	}

	private List<Parameter> readParams(String section, ParameterScope scope,
			boolean input) {
		int row = findSection(section);
		if (row < 0) {
			return Collections.emptyList();
		}
		List<Parameter> parameters = new ArrayList<>();
		row += 2;
		while (true) {
			String name = config.getString(sheet, row, 0);
			if (Strings.isNullOrEmpty(name)) {
				break;
			}
			Parameter parameter = input ? readInputParam(row, name, scope)
					: readDependentParam(row, name, scope);
			parameters.add(parameter);
			row++;
		}
		return parameters;
	}

	private Parameter readDependentParam(int row, String name,
			ParameterScope scope) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setInputParameter(false);
		parameter.setScope(scope);
		parameter.setFormula(config.getString(sheet, row, 1));
		parameter.setValue(config.getDouble(sheet, row, 2));
		parameter.setDescription(config.getString(sheet, row, 3));
		return parameter;
	}

	private Parameter readInputParam(int row, String name, ParameterScope scope) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setInputParameter(true);
		parameter.setScope(scope);
		parameter.setValue(config.getDouble(sheet, row, 1));
		parameter.setUncertainty(config.getUncertainty(sheet, row, 2));
		parameter.setDescription(config.getString(sheet, row, 7));
		return parameter;
	}

	private int findSection(String section) {
		if (section == null) {
			return -1;
		}
		for (int i = 0; i < MAX_ROWS; i++) {
			String s = config.getString(sheet, i, 0);
			if (s == null) {
				continue;
			}
			if (section.equalsIgnoreCase(s.trim())) {
				return i;
			}
		}
		return -1;
	}
}
