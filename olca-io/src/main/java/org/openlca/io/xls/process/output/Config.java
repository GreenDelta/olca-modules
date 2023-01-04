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
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.util.Strings;

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
		dateStyle.setAlignment(HorizontalAlignment.LEFT);
		pairHeader = workbook.createCellStyle();
		pairHeader.setVerticalAlignment(VerticalAlignment.TOP);
		pairValue = workbook.createCellStyle();
		pairValue.setWrapText(true);
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

	void uncertainty(Sheet sheet, int row, int col, Uncertainty u) {
		if (u == null || u.distributionType == UncertaintyType.NONE) {
			Excel.cell(sheet, row, col, "undefined");
			return;
		}
		switch (u.distributionType) {
			case LOG_NORMAL -> {
				Excel.cell(sheet, row, col, "log-normal");
				param(sheet, u.parameter1,  row, col + 1);
				param(sheet, u.parameter2,  row, col + 2);
			}
			case NORMAL -> {
				Excel.cell(sheet, row, col, "normal");
				param(sheet, u.parameter1,  row, col + 1);
				param(sheet, u.parameter2,  row, col + 2);
			}
			case TRIANGLE -> {
				Excel.cell(sheet, row, col, "triangular");
				param(sheet, u.parameter1,  row, col + 3);
				param(sheet, u.parameter2,  row, col + 1);
				param(sheet, u.parameter3,  row, col + 4);
			}
			case UNIFORM -> {
				Excel.cell(sheet, row, col, "uniform");
				param(sheet, u.parameter1,  row, col + 3);
				param(sheet, u.parameter2,  row, col + 4);
			}
			default -> {
			}
		}
	}

	private void param(Sheet sheet, Double value, int row, int col) {
		if (value != null) {
			Excel.cell(sheet, row, col, value);
		}
	}

	static void sort(List<? extends RefEntity> list) {
		list.sort((e1, e2) -> {
			if (e1 == null && e2 == null)
				return 0;
			if (e1 == null)
				return -1;
			if (e2 == null)
				return 1;
			int c = Strings.compare(e1.name, e2.name);
			if (c != 0)
				return c;
			if (e1 instanceof RootEntity re1 && e2 instanceof RootEntity re2) {
				var c1 = CategoryPath.getFull(re1.category);
				var c2 = CategoryPath.getFull(re2.category);
				return Strings.compare(c1, c2);
			}
			return 0;
		});
	}
}
