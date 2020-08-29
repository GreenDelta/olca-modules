package org.openlca.io.xls.process.output;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

class ParameterSheet {

	private final Config config;
	private final Sheet sheet;

	private int row = 0;

	private ParameterSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Parameters");
	}

	public static void write(Config config) {
		new ParameterSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 7);
		writeGlobalParams();
		config.header(sheet, row++, 0, "Input parameters");
		writeInputParams(getInputParameters());
		config.header(sheet, row++, 0, "Calculated parameters");
		writeDependentParams(getDependentParameters());
		Excel.autoSize(sheet, 0, 7);
	}

	private void writeGlobalParams() {
		ParameterDao dao = new ParameterDao(config.database);
		List<Parameter> all = dao.getGlobalParameters();
		all.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
		List<Parameter> inputParams = new ArrayList<>();
		List<Parameter> calcParams = new ArrayList<>();
		for (Parameter p : all) {
			if (p.isInputParameter)
				inputParams.add(p);
			else
				calcParams.add(p);
		}
		config.header(sheet, row++, 0, "Global input parameters");
		writeInputParams(inputParams);
		config.header(sheet, row++, 0, "Global calculated parameters");
		writeDependentParams(calcParams);
	}

	private void writeInputParams(List<Parameter> params) {
		writeInputHeader();
		for (Parameter param : params) {
			row++;
			Excel.cell(sheet, row, 0, param.name);
			Excel.cell(sheet, row, 1, param.value);
			config.uncertainty(sheet, row, 2, param.uncertainty);
			Excel.cell(sheet, row, 7, param.description);
		}
		row += 2;
	}

	private void writeInputHeader() {
		config.header(sheet, row, 0, "Name");
		config.header(sheet, row, 1, "Value");
		config.header(sheet, row, 2, "Uncertainty");
		config.header(sheet, row, 3, "(g)mean | mode");
		config.header(sheet, row, 4, "SD | GSD");
		config.header(sheet, row, 5, "Minimum");
		config.header(sheet, row, 6, "Maximum");
		config.header(sheet, row, 7, "Description");
	}

	private void writeDependentParams(List<Parameter> params) {
		writeDependentHeader();
		for (Parameter param : params) {
			row++;
			Excel.cell(sheet, row, 0, param.name);
			Excel.cell(sheet, row, 1, param.formula);
			Excel.cell(sheet, row, 2, param.value);
			Excel.cell(sheet, row, 3, param.description);
		}
		row += 2;
	}

	private void writeDependentHeader() {
		config.header(sheet, row, 0, "Name");
		config.header(sheet, row, 1, "Formula");
		config.header(sheet, row, 2, "Value");
		config.header(sheet, row, 3, "Description");
	}

	private List<Parameter> getInputParameters() {
		var params = new ArrayList<Parameter>();
		for (var param : config.process.parameters) {
			if (param.isInputParameter)
				params.add(param);
		}
		params.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
		return params;
	}

	private List<Parameter> getDependentParameters() {
		var params = new ArrayList<Parameter>();
		for (var param : config.process.parameters) {
			if (!param.isInputParameter)
				params.add(param);
		}
		params.sort((p1, p2) -> Strings.compare(p1.name, p2.name));
		return params;
	}
}
