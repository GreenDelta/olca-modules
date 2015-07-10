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
			readGlobals();
			List<Parameter> params = config.process.getParameters();
			params.addAll(readParams("Input parameters",
					ParameterScope.PROCESS, true));
			params.addAll(readParams("Calculated parameters",
					ParameterScope.PROCESS, false));
		} catch (Exception e) {
			log.error("failed to read parameter sheet", e);
		}
	}

	private void readGlobals() {
		List<Parameter> globals = readParams(
				"Global input parameters",
				ParameterScope.GLOBAL, true);
		globals.addAll(readParams(
				"Global calculated parameters",
				ParameterScope.GLOBAL, false));
		syncGlobals(globals);
	}

	private void syncGlobals(List<Parameter> sheetParams) {
		ParameterDao dao = new ParameterDao(config.database);
		List<Parameter> globals = new ArrayList<>();
		globals.addAll(dao.getGlobalParameters());
		for (Parameter p : sheetParams) {
			boolean found = false;
			for (Parameter global : globals) {
				String name = global.getName();
				if (name == null)
					continue;
				if (name.equalsIgnoreCase(p.getName())) {
					found = true;
					break;
				}
			}
			if (!found)
				globals.add(dao.insert(p));
		}
	}

	private List<Parameter> readParams(String section, ParameterScope scope,
			boolean input) {
		int row = findSection(section);
		if (row < 0)
			return Collections.emptyList();
		List<Parameter> list = new ArrayList<>();
		row += 2;
		while (true) {
			String name = config.getString(sheet, row, 0);
			if (Strings.isNullOrEmpty(name))
				break;
			Parameter p = input ? readInputParam(row, name, scope)
					: readDependentParam(row, name, scope);
			list.add(p);
			row++;
		}
		return list;
	}

	private Parameter readDependentParam(int row, String name,
			ParameterScope scope) {
		Parameter p = new Parameter();
		p.setName(name);
		p.setInputParameter(false);
		p.setScope(scope);
		p.setFormula(config.getString(sheet, row, 1));
		p.setValue(config.getDouble(sheet, row, 2));
		p.setDescription(config.getString(sheet, row, 3));
		return p;
	}

	private Parameter readInputParam(int row, String name,
			ParameterScope scope) {
		Parameter p = new Parameter();
		p.setName(name);
		p.setInputParameter(true);
		p.setScope(scope);
		p.setValue(config.getDouble(sheet, row, 1));
		p.setUncertainty(config.getUncertainty(sheet, row, 2));
		p.setDescription(config.getString(sheet, row, 7));
		return p;
	}

	private int findSection(String section) {
		if (section == null)
			return -1;
		for (int i = 0; i < MAX_ROWS; i++) {
			String s = config.getString(sheet, i, 0);
			if (s == null)
				continue;
			if (section.equalsIgnoreCase(s.trim()))
				return i;
		}
		return -1;
	}
}
