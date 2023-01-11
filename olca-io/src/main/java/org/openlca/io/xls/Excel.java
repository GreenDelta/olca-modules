package org.openlca.io.xls;

import java.util.Date;
import java.util.Optional;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for Excel exports.
 */
public class Excel {

	public static final int MAX_COLUMN_INDEX = SpreadsheetVersion.EXCEL2007.getLastColumnIndex();

	private Excel() {
	}

	public static int width(int pixel) {
		int[] offsetMap = {0, 36, 73, 109, 146, 182, 219};
		short widthUnits = (short) (256 * (pixel / 7));
		widthUnits += offsetMap[(pixel % 7)];
		return widthUnits;
	}

	public static void bold(Workbook wb, Sheet sheet, int row, int col) {
		var cell = cell(sheet, row, col);
		if (cell.isEmpty())
			return;
		var bold = createBoldStyle(wb);
		cell.get().setCellStyle(bold);
	}

	public static Optional<Cell> cell(Sheet sheet, int row, int column) {
		Row _row = row(sheet, row);
		return cell(_row, column);
	}

	public static CellStyle createBoldStyle(Workbook workbook) {
		var headerStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		return headerStyle;
	}

	public static CellStyle dateStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setDataFormat(dateFormat(workbook));
		return style;
	}

	public static short dateFormat(Workbook workbook) {
		DataFormat format = workbook.createDataFormat();
		return format.getFormat("mm/dd/yyyy hh:mm");
	}

	public static Row row(Sheet sheet, int row) {
		Row _row = sheet.getRow(row);
		if (_row == null)
			_row = sheet.createRow(row);
		return _row;
	}

	public static Optional<Cell> cell(Row row, int column) {
		if (column > MAX_COLUMN_INDEX)
			return Optional.empty();
		var cell = row.getCell(column);
		if (cell == null) {
			cell = row.createCell(column);
		}
		return Optional.of(cell);
	}

	public static Optional<Cell> cell(Sheet sheet, int row, int column, String value) {
		Row _row = row(sheet, row);
		return cell(_row, column, value);
	}

	public static Optional<Cell> cell(Row row, int column, String value) {
		var cell = cell(row, column);
		if (cell.isEmpty())
			return Optional.empty();
		// set a default value if NULL > otherwise auto-size fails for XSSF
		cell.get().setCellValue(value == null ? "" : value);
		return cell;
	}

	public static Optional<Cell> cell(Sheet sheet, int row, int column, double value) {
		Row _row = row(sheet, row);
		return cell(_row, column, value);
	}

	public static Optional<Cell> cell(Row row, int column, double value) {
		var cell = cell(row, column);
		if (cell.isEmpty())
			return Optional.empty();
		cell.get().setCellValue(value);
		return cell;
	}

	public static Optional<Cell> cell(Sheet sheet, int row, int col, boolean value) {
		var cell = cell(sheet, row, col);
		if (cell.isEmpty())
			return Optional.empty();
		cell.get().setCellValue(value);
		return cell;
	}

	/**
	 * In order to call `autoSize` on columns of an SXSSF sheet, these sizes
	 * of these columns need to be tracked.
	 */
	public static void trackSize(Sheet sheet, int from, int to) {
		if (!(sheet instanceof SXSSFSheet sxssf))
			return;
		for (int col = from; col <= to; col++) {
			sxssf.trackColumnForAutoSizing(col);
		}
	}

	/**
	 * Call `autoSize` on the columns of the given interval (including the end of
	 * interval) In case of an SXSSF it is required to track the column sizes before
	 * calling auto-size on them, see `trackSize`.
	 */
	public static void autoSize(Sheet sheet, int from, int to) {
		if (sheet == null)
			return;
		// in case of an SXSSF sheet we make sure that the columns
		// of the given range have been tracked, otherwise calling
		// auto-size on them will crash with an exception.
		if (sheet instanceof SXSSFSheet sxssf) {
			var tracked = sxssf.getTrackedColumnsForAutoSizing();
			for (int col = from; col <= to; col++) {
				if (!tracked.contains(col))
					continue;
				sxssf.autoSizeColumn(col);
			}
			return;
		}
		for (int column = from; column <= to; column++) {
			sheet.autoSizeColumn(column);
		}
	}

	public static String getString(Sheet sheet, int row, int col) {
		return sheet != null
				? getString(sheet.getRow(row), col)
				: null;
	}

	public static String getString(Row row, int col) {
		return row != null
				? getString(row.getCell(col))
				: null;
	}

	public static String getString(Cell cell) {
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

	public static Date getDate(Sheet sheet, int row, int col) {
		return sheet != null
				? getDate(sheet.getRow(row), col)
				: null;
	}

	public static Date getDate(Row row, int col) {
		return row != null
				? getDate(row.getCell(col))
				: null;
	}

	public static Date getDate(Cell cell) {
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

	public static double getDouble(Sheet sheet, int row, int col) {
		var _row = sheet.getRow(row);
		if (_row == null)
			return 0d;
		return getDouble(_row.getCell(col));
	}

	public static double getDouble(Cell cell) {
		if (cell == null)
			return 0;
		try {
			return cell.getNumericCellValue();
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Returns the value of the given cell which might be {@code null} when this
	 * is a blank cell.
	 */
	public static Object getValue(Cell cell) {
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

}
