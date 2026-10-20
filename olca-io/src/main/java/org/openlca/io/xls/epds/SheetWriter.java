package org.openlca.io.xls.epds;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

/// Writes the rows of a sheet in the standard EPD layout: section headers in
/// column A, field names in column B, values in column C, and units in
/// column D.
class SheetWriter {

	private final Sheet sheet;
	private final Styles styles;
	private int cursor;

	SheetWriter(Sheet sheet, Styles styles) {
		this.sheet = sheet;
		this.styles = styles;
	}

	SheetWriter section(Section section) {
		if (section == null)
			return this;
		var cell = cell(cursor, 0);
		cell.setCellValue(section.header());
		cell.setCellStyle(styles.bold());
		cursor++;
		return this;
	}

	SheetWriter field(Field field, String value) {
		label(field);
		if (value != null) {
			var cell = cell(cursor, 2);
			cell.setCellValue(value);
			cell.setCellStyle(styles.wrap());
		}
		cursor++;
		return this;
	}

	SheetWriter field(Field field, Double value, String unit) {
		label(field);
		if (value != null) {
			var cell = cell(cursor, 2);
			cell.setCellValue(value);
			cell.setCellStyle(styles.plain());
		}
		if (unit != null) {
			var cell = cell(cursor, 3);
			cell.setCellValue(unit);
			cell.setCellStyle(styles.plain());
		}
		cursor++;
		return this;
	}

	SheetWriter field(Field field, Date date) {
		return field(field, date, false);
	}

	SheetWriter dateTime(Field field, Date date) {
		return field(field, date, true);
	}

	private SheetWriter field(Field field, Date date, boolean withTime) {
		label(field);
		if (date != null) {
			var cell = cell(cursor, 2);
			cell.setCellValue(date);
			cell.setCellStyle(withTime
				? styles.dateTime()
				: styles.date());
		}
		cursor++;
		return this;
	}

	SheetWriter blank() {
		cursor++;
		return this;
	}

	private void label(Field field) {
		if (field == null)
			return;
		var cell = cell(cursor, 1);
		cell.setCellValue(field.label());
		cell.setCellStyle(styles.plain());
	}

	private Cell cell(int row, int col) {
		var r = sheet.getRow(row);
		if (r == null) {
			r = sheet.createRow(row);
		}
		var cell = r.getCell(col);
		return cell == null
			? r.createCell(col)
			: cell;
	}
}
