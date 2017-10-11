package org.openlca.io.xls.process.output;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.io.xls.Excel;

class Config {

	final IDatabase database;
	final Workbook workbook;
	final Process process;
	final CellStyle headerStyle;
	final CellStyle dateStyle;

	private final CellStyle pairHeader;
	private final CellStyle pairValue;

	Config(Workbook workbook, IDatabase database, Process process) {
		this.workbook = workbook;
		this.database = database;
		this.process = process;
		headerStyle = Excel.headerStyle(workbook);
		dateStyle = Excel.dateStyle(workbook);
		dateStyle.setAlignment(CellStyle.ALIGN_LEFT);
		pairHeader = workbook.createCellStyle();
		pairHeader.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		pairValue = workbook.createCellStyle();
		pairValue.setWrapText(true);
	}

	void header(Sheet sheet, int row, int col, String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(headerStyle);
	}

	void date(Sheet sheet, int row, int col, long time) {
		if (time == 0)
			return;
		Cell cell = Excel.cell(sheet, row, col);
		cell.setCellValue(new Date(time));
		cell.setCellStyle(dateStyle);
	}

	void date(Sheet sheet, int row, int col, Date date) {
		if (date == null)
			return;
		Cell cell = Excel.cell(sheet, row, col);
		cell.setCellValue(date);
		cell.setCellStyle(dateStyle);
	}

	void pair(Sheet sheet, int row, String header, String value) {
		Excel.cell(sheet, row, 0, header).setCellStyle(pairHeader);
		Excel.cell(sheet, row, 1, value).setCellStyle(pairValue);
	}

	void uncertainty(Sheet sheet, int row, int col, Uncertainty uncertainty) {
		if (uncertainty == null
				|| uncertainty.getDistributionType() == UncertaintyType.NONE) {
			Excel.cell(sheet, row, col, "undefined");
			return;
		}
		switch (uncertainty.getDistributionType()) {
			case LOG_NORMAL:
				Excel.cell(sheet, row, col, "log-normal");
				param1(uncertainty, sheet, row, col + 1);
				param2(uncertainty, sheet, row, col + 2);
				break;
			case NORMAL:
				Excel.cell(sheet, row, col, "normal");
				param1(uncertainty, sheet, row, col + 1);
				param2(uncertainty, sheet, row, col + 2);
				break;
			case TRIANGLE:
				Excel.cell(sheet, row, col, "triangular");
				param1(uncertainty, sheet, row, col + 3);
				param2(uncertainty, sheet, row, col + 1);
				param3(uncertainty, sheet, row, col + 4);
				break;
			case UNIFORM:
				Excel.cell(sheet, row, col, "uniform");
				param1(uncertainty, sheet, row, col + 3);
				param2(uncertainty, sheet, row, col + 4);
				break;
			default:
				break;
		}
	}

	private void param1(Uncertainty uncertainty, Sheet sheet, int row, int col) {
		String formula = uncertainty.getParameter1Formula();
		Double value = uncertainty.getParameter1Value();
		param(formula, value, sheet, row, col);
	}

	private void param2(Uncertainty uncertainty, Sheet sheet, int row, int col) {
		String formula = uncertainty.getParameter2Formula();
		Double value = uncertainty.getParameter2Value();
		param(formula, value, sheet, row, col);
	}

	private void param3(Uncertainty uncertainty, Sheet sheet, int row, int col) {
		String formula = uncertainty.getParameter3Formula();
		Double value = uncertainty.getParameter3Value();
		param(formula, value, sheet, row, col);
	}

	private void param(String formula, Double value, Sheet sheet, int row, int col) {
		if (formula != null)
			Excel.cell(sheet, row, col, formula);
		else if (value != null)
			Excel.cell(sheet, row, col, value);
	}
}
