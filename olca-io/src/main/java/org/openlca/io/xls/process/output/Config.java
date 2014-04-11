package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.io.xls.Excel;

import java.util.Date;

class Config {

	final IDatabase database;
	final Workbook workbook;
	final Process process;
	final CellStyle headerStyle;
	final CellStyle dateStyle;

	Config(Workbook workbook, IDatabase database, Process process) {
		this.workbook = workbook;
		this.database = database;
		this.process = process;
		this.headerStyle = Excel.headerStyle(workbook);
		this.dateStyle = Excel.dateStyle(workbook);
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
}
