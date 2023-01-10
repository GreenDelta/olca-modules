package org.openlca.io.xls.process.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;

import com.google.common.base.Strings;

class ParameterSheet {

	/**
	 * The maximum number of rows for searching for a parameter section in this
	 * sheet.
	 */
	private final int MAX_ROWS = 5000;

	private final ProcessWorkbook wb;
	private final Sheet sheet;

	private ParameterSheet(ProcessWorkbook wb) {
		this.wb = wb;
		sheet = wb.getSheet("Parameters");
	}

	public static void read(ProcessWorkbook wb) {
		new ParameterSheet(wb).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		readGlobals();
		List<Parameter> params = wb.process.parameters;
		params.addAll(readParams("Input parameters",
				ParameterScope.PROCESS, true));
		params.addAll(readParams("Calculated parameters",
				ParameterScope.PROCESS, false));
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
		ParameterDao dao = new ParameterDao(wb.db);
		List<Parameter> globals = new ArrayList<>(dao.getGlobalParameters());
		for (Parameter p : sheetParams) {
			boolean found = false;
			for (Parameter global : globals) {
				String name = global.name;
				if (name == null)
					continue;
				if (name.equalsIgnoreCase(p.name)) {
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
			String name = wb.getString(sheet, row, 0);
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
		p.name = name;
		p.isInputParameter = false;
		p.scope = scope;
		p.formula = wb.getString(sheet, row, 1);
		p.value = wb.getDouble(sheet, row, 2);
		p.description = wb.getString(sheet, row, 3);
		return p;
	}

	private Parameter readInputParam(int row, String name,
			ParameterScope scope) {
		Parameter p = new Parameter();
		p.name = name;
		p.isInputParameter = true;
		p.scope = scope;
		p.value = wb.getDouble(sheet, row, 1);
		p.uncertainty = wb.getUncertainty(sheet, row, 2);
		p.description = wb.getString(sheet, row, 7);
		return p;
	}

	private int findSection(String section) {
		if (section == null)
			return -1;
		for (int i = 0; i < MAX_ROWS; i++) {
			String s = wb.getString(sheet, i, 0);
			if (s == null)
				continue;
			if (section.equalsIgnoreCase(s.trim()))
				return i;
		}
		return -1;
	}
}
