package org.openlca.io.xls;

import java.util.Optional;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Helper methods for Excel exports. */
public class Excel {

	public static final int MAX_COLUMN_INDEX = SpreadsheetVersion.EXCEL2007.getLastColumnIndex();

	private Excel() {
	}

	public static int width(int pixel) {
		int[] offsetMap = { 0, 36, 73, 109, 146, 182, 219 };
		short widthUnits = (short) (256 * (pixel / 7));
		widthUnits += offsetMap[(pixel % 7)];
		return widthUnits;
	}

	public static void headerStyle(Workbook workbook, Sheet sheet, int row,
			int column) {
		var cell = cell(sheet, row, column);
		if (cell.isEmpty())
			return;
		cell.get().setCellStyle(headerStyle(workbook));
	}

	public static Optional<Cell> cell(Sheet sheet, int row, int column) {
		Row _row = row(sheet, row);
		return cell(_row, column);
	}

	public static CellStyle headerStyle(Workbook workbook) {
		CellStyle headerStyle = workbook.createCellStyle();
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
		if (!(sheet instanceof SXSSFSheet))
			return;
		var sxssf = (SXSSFSheet) sheet;
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
		if (sheet instanceof SXSSFSheet) {
			var sxssf = (SXSSFSheet) sheet;
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
		try {
			Row _row = sheet.getRow(row);
			if (_row == null)
				return null;
			Cell cell = _row.getCell(col);
			if (cell == null)
				return null;
			return cell.getStringCellValue();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Excel.class);
			log.error("Failed to get string", e);
			return null;
		}
	}

	public static double getDouble(Sheet sheet, int row, int col) {
		try {
			Row _row = sheet.getRow(row);
			if (_row == null)
				return 0d;
			Cell cell = _row.getCell(col);
			if (cell == null)
				return 0d;
			return cell.getNumericCellValue();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Excel.class);
			log.error("Failed to get double", e);
			return 0d;
		}
	}

}
