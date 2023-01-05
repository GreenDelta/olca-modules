package org.openlca.io.xls.process.output;

import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.io.xls.Excel;

class ProcessWorkbook {

	final IDatabase db;
	final Workbook workbook;
	final Process process;
	final CellStyle headerStyle;
	final CellStyle dateStyle;

	private final CellStyle pairHeader;
	private final CellStyle pairValue;

	private final List<EntitySheet> entitySheets;

	ProcessWorkbook(Workbook wb, IDatabase db, Process process) {
		this.workbook = wb;
		this.db = db;
		this.process = process;

		// styles
		headerStyle = Excel.headerStyle(wb);
		dateStyle = Excel.dateStyle(wb);
		dateStyle.setAlignment(HorizontalAlignment.LEFT);
		pairHeader = wb.createCellStyle();
		pairHeader.setVerticalAlignment(VerticalAlignment.TOP);
		pairValue = wb.createCellStyle();
		pairValue.setWrapText(true);

		// sheets for referenced dependencies
		entitySheets = List.of(
				new ActorSheet(this),
				new SourceSheet(this),
				new FlowPropertyFactorSheet(this));
	}

	void write() {
		new InfoSheet(this).write();
		new AdminInfoSheet(this).write();
		IOSheet.writeInputs(this);
		IOSheet.writeOutputs(this);
		ParameterSheet.write(this);
		AllocationSheet.write(this);
		ModelingSheet.write(this);

		for (var sheet : entitySheets) {
			sheet.flush();
		}
	}

	void visit(RootEntity e) {
		for (var sheet : entitySheets) {
			sheet.visit(e);
		}
	}

	Sheet createSheet(String name) {
		return workbook.createSheet(name);
	}

	void header(Sheet sheet, int row, int col, String val) {
		var cell = Excel.cell(sheet, row, col, val);
		cell.ifPresent(value -> value.setCellStyle(headerStyle));
	}

	void date(Sheet sheet, int row, int col, long time) {
		if (time == 0)
			return;
		date(sheet, row, col, new Date(time));
	}

	void date(Sheet sheet, int row, int col, Date date) {
		if (date == null)
			return;
		var cell = Excel.cell(sheet, row, col);
		cell.ifPresent(c -> {
			c.setCellValue(date);
			c.setCellStyle(dateStyle);
		});
	}

	void pair(Sheet sheet, int row, String header, String value) {
		Excel.cell(sheet, row, 0, header)
				.ifPresent(c -> c.setCellStyle(pairHeader));
		Excel.cell(sheet, row, 1, value)
				.ifPresent(c -> c.setCellStyle(pairValue));
	}
}
