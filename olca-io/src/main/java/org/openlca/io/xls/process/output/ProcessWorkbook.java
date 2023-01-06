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
import org.openlca.io.CategoryPath;
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

		// sheets for referenced entities
		entitySheets = List.of(
				new ActorSheet(this),
				new SourceSheet(this),
				new LocationSheet(this),
				new FlowSheet(this),
				new FlowPropertySheet(this),
				new FlowPropertyFactorSheet(this),
				new UnitGroupSheet(this),
				new UnitSheet(this));
	}

	void write() {
		new InfoSheet(this).write();
		IOSheet.writeInputs(this);
		IOSheet.writeOutputs(this);
		new AdminInfoSheet(this).write();
		new ModelingSheet(this).write();
		ParameterSheet.write(this);
		AllocationSheet.write(this);

		for (var sheet : entitySheets) {
			sheet.flush();
		}
	}

	void visit(RootEntity e) {
		if (e == null)
			return;
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

	SheetCursor createCursor(String name) {
		var sheet = createSheet(name);
		sheet.setColumnWidth(1, 20 * 256);
		sheet.setColumnWidth(1, 50 * 256);
		return new SheetCursor(sheet);
	}

	class SheetCursor {
		private final Sheet sheet;
		private int row;

		private SheetCursor(Sheet sheet) {
			this.sheet = sheet;
		}

		SheetCursor pair(String header, RootEntity e) {
			Excel.cell(sheet, row, 0, header)
					.ifPresent(c -> c.setCellStyle(pairHeader));
			if (e != null) {
				visit(e);
				Excel.cell(sheet, row, 1, e.name)
						.ifPresent(c -> c.setCellStyle(pairValue));
				Excel.cell(sheet, row, 2, CategoryPath.getFull(e.category))
						.ifPresent(c -> c.setCellStyle(pairValue));
			}
			row++;
			return this;
		}

		SheetCursor next(RootEntity e) {
			if (e == null)
				return this;
			visit(e);
			Excel.cell(sheet, row, 0, e.name);
			Excel.cell(sheet, row, 1, CategoryPath.getFull(e.category));
			row++;
			return this;
		}

		SheetCursor pair(String header, String value) {
			Excel.cell(sheet, row, 0, header)
					.ifPresent(c -> c.setCellStyle(pairHeader));
			Excel.cell(sheet, row, 1, value)
					.ifPresent(c -> c.setCellStyle(pairValue));
			row++;
			return this;
		}

		SheetCursor pair(String header, boolean value) {
			Excel.cell(sheet, row, 0, header)
					.ifPresent(c -> c.setCellStyle(pairHeader));
			Excel.cell(sheet, row, 1, value)
					.ifPresent(c -> c.setCellStyle(pairValue));
			row++;
			return this;
		}

		SheetCursor pair(String header, Date date) {
			Excel.cell(sheet, row, 0, header)
					.ifPresent(c -> c.setCellStyle(pairHeader));
			if (date != null) {
				Excel.cell(sheet, row, 1)
						.ifPresent(c -> {
							c.setCellValue(date);
							c.setCellStyle(dateStyle);
						});
			}
			row++;
			return this;
		}

		SheetCursor pairDate(String header, long time) {
			return pair(header, time != 0 ? new Date(time) : null);
		}

		SheetCursor header(String header) {
			Excel.cell(sheet, row, 0, header)
					.ifPresent(cell -> cell.setCellStyle(headerStyle));
			row++;
			return this;
		}

		SheetCursor empty() {
			row++;
			return this;
		}

	}
}
