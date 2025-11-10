package org.openlca.io.xls.process;

import java.util.Date;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.commons.Strings;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;

final class In {

	private In() {
	}

	static Row row(Sheet sheet, int row) {
		return sheet != null
			? sheet.getRow(row)
			:  null;
	}

	static Cell cell(Sheet sheet, int row, int col) {
		return cell(row(sheet, row), col);
	}

	static Cell cell(Row row, int col) {
		return row != null
			? row.getCell(col)
			: null;
	}

	static String stringOf(Sheet sheet, int row, int col) {
		return stringOf(cell(sheet, row, col));
	}

	static String stringOf(Row row, int col) {
		return stringOf(cell(row, col));
	}

	static String stringOf(Cell cell) {
		if (cell == null)
			return null;
		try {
			var type = cell.getCellType();
			if (type == null)
				return null;
			return switch (type) {
				case STRING -> cell.getStringCellValue();
				case ERROR -> "Error: " + cell.getErrorCellValue();
				case BOOLEAN -> cell.getBooleanCellValue()
					? "true"
					: "false";
				case FORMULA -> cell.getCellFormula();
				case NUMERIC -> Double.toString(cell.getNumericCellValue());
				case BLANK, _NONE -> null;
			};
		} catch (Exception e) {
			return null;
		}
	}

	static Date dateOf(Cell cell) {
		if (cell == null)
			return null;
		try {
			var type = cell.getCellType();
			if (type != CellType.NUMERIC)
				return null;
			return cell.getDateCellValue();
		} catch (Exception e) {
			return null;
		}
	}

	static double doubleOf(Cell cell) {
		if (cell == null)
			return 0;
		try {
			return cell.getNumericCellValue();
		} catch (Exception e) {
			return 0;
		}
	}

	static Object valueOf(Cell cell) {
		if (cell == null)
			return null;
		var type = cell.getCellType();
		if (type == null)
			return null;
		try {
			return switch (type) {
				case ERROR -> "error: " + cell.getErrorCellValue();
				case STRING -> cell.getStringCellValue();
				case BOOLEAN -> cell.getBooleanCellValue();
				case FORMULA -> cell.getCellFormula();
				case NUMERIC -> cell.getNumericCellValue();
				case BLANK, _NONE -> null;
			};
		} catch (Exception e) {
			return null;
		}
	}

	static boolean booleanOf(Cell cell) {
		var val = valueOf(cell);
		if (val == null)
			return false;
		if (val instanceof Boolean b)
			return b;
		if (val instanceof Number n)
			return n.doubleValue() != 0;
		if (val instanceof String s) {
			return switch (s.trim().toLowerCase()) {
				case "y", "yes", "x", "ok" -> true;
				default -> false;
			};
		}
		return false;
	}

	static void mapBase(CellReader r, RefEntity e) {
		if (r == null || e == null)
			return;
		e.refId = r.str(Field.UUID);
		if (Strings.isBlank(e.refId)) {
			e.refId = UUID.randomUUID().toString();
		}
		e.name = r.str(Field.NAME);
		e.description = r.str(Field.DESCRIPTION);
		if (e instanceof RootEntity root) {
			//var path = fields.str(row, Field.CATEGORY);
			//var type = ModelType.of(
			var version = r.str(Field.VERSION);
			root.version = Version.fromString(version).getValue();
			root.tags = r.str(Field.TAGS);
			var lastChange = r.date(Field.LAST_CHANGE);
			if (lastChange != null) {
				root.lastChange = lastChange.getTime();
			}
		}
	}

}
