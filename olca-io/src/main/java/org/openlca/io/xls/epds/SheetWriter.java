package org.openlca.io.xls.epds;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.util.Categories;

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
		return section != null
			? section(section.header())
			: this;
	}

	SheetWriter section(String header) {
		var cell = cell(cursor, 0);
		cell.setCellValue(header == null ? "" : header);
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

	void blank() {
		cursor++;
	}

	SheetWriter metaDataFieldsOf(RootEntity entity) {
		if (entity == null)
			return this;

		var lastChange = entity.lastChange > 0
			? new Date(entity.lastChange)
			: null;

		return field(Field.UUID, entity.refId)
			.field(Field.CATEGORY, categoryOf(entity))
			.field(Field.VERSION, Version.asString(entity.version))
			.field(Field.LAST_CHANGE, lastChange, true)
			.field(Field.TAGS, entity.tags)
			.field(Field.DESCRIPTION, entity.description);
	}

	private static String categoryOf(RootEntity entity) {
		if (entity.category == null)
			return null;
		var path = Categories.path(entity.category);
		return path.isEmpty()
			? null
			: String.join("/", path);
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
