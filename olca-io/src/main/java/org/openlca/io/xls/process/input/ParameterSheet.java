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

	public static void read(final Config config) {
		new ParameterSheet(config).read();
	}

	private final Config config;

	private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * The maximum number of rows for searching for a parameter section in this
	 * sheet.
	 */
	private final int MAX_ROWS = 5000;

	private final Sheet sheet;

	private ParameterSheet(final Config config) {
		this.config = config;
		sheet = config.workbook.getSheet("Parameters");
	}

	private int findSection(final String section) {
		if (section == null) {
			return -1;
		}
		for (int i = 0; i < MAX_ROWS; i++) {
			final String s = config.getString(sheet, i, 0);
			if (s == null) {
				continue;
			}
			if (section.equalsIgnoreCase(s.trim())) {
				return i;
			}
		}
		return -1;
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read parameters");
			final List<Parameter> globalParams = readParams(
					"Global parameters", ParameterScope.GLOBAL, true);
			syncGlobals(globalParams);
			final List<Parameter> params = config.process.getParameters();
			params.addAll(readParams("Input parameters",
					ParameterScope.PROCESS, true));
			params.addAll(readParams("Dependent parameters",
					ParameterScope.PROCESS, false));
		} catch (final Exception e) {
			log.error("failed to read parameter sheet", e);
		}
	}

	private Parameter readDependentParam(final int row, final String name,
			final ParameterScope scope) {
		final Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setInputParameter(false);
		parameter.setScope(scope);
		parameter.setFormula(config.getString(sheet, row, 1));
		parameter.setValue(config.getDouble(sheet, row, 2));
		parameter.setDescription(config.getString(sheet, row, 3));
		return parameter;
	}

	private Parameter readInputParam(final int row, final String name,
			final ParameterScope scope) {
		final Parameter parameter = new Parameter();
		parameter.setName(name);
		parameter.setInputParameter(true);
		parameter.setScope(scope);
		parameter.setValue(config.getDouble(sheet, row, 1));
		parameter.setUncertainty(config.getUncertainty(sheet, row, 2));
		parameter.setDescription(config.getString(sheet, row, 7));
		return parameter;
	}

	private List<Parameter> readParams(final String section,
			final ParameterScope scope, final boolean input) {
		int row = findSection(section);
		if (row < 0) {
			return Collections.emptyList();
		}
		final List<Parameter> parameters = new ArrayList<>();
		row += 2;
		while (true) {
			final String name = config.getString(sheet, row, 0);
			if (Strings.isNullOrEmpty(name)) {
				break;
			}
			final Parameter parameter = input ? readInputParam(row, name, scope)
					: readDependentParam(row, name, scope);
			parameters.add(parameter);
			row++;
		}
		return parameters;
	}

	private void syncGlobals(final List<Parameter> sheetParams) {
		final ParameterDao dao = new ParameterDao(config.database);
		final List<Parameter> globals = new ArrayList<>();
		globals.addAll(dao.getGlobalParameters());
		for (final Parameter sheetParam : sheetParams) {
			boolean found = false;
			for (final Parameter global : globals) {
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
}
