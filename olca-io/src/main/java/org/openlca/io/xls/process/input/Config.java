package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.io.Categories;

import java.util.Date;

class Config {

	final IDatabase database;
	final Workbook workbook;
	final Process process;
	final RefData refData;

	Config(Workbook workbook, IDatabase database, Process process) {
		this.workbook = workbook;
		this.database = database;
		this.process = process;
		this.refData = new RefData();
	}

	String getString(Sheet sheet, int row, int col) {
		Cell cell = getCell(sheet, row, col);
		if (cell == null)
			return null;
		try {
			String s = cell.getStringCellValue();
			return s != null ? s.trim() : null;
		} catch (Exception e) {
			// we do not use the cell type check but try it the hard way
			// instead because the cell type may be of formula type which
			// will return a string
			return null;
		}
	}

	Date getDate(Sheet sheet, int row, int col) {
		Cell cell = getCell(sheet, row, col);
		if (cell == null)
			return null;
		try {
			return cell.getDateCellValue();
		} catch (Exception e) {
			return null;
		}
	}

	double getDouble(Sheet sheet, int row, int col) {
		Cell cell = getCell(sheet, row, col);
		if (cell == null)
			return 0;
		try {
			return cell.getNumericCellValue();
		} catch (Exception e) {
			return 0;
		}
	}

	Cell getCell(Sheet sheet, int row, int col) {
		if (sheet == null)
			return null;
		Row xrow = sheet.getRow(row);
		if (xrow == null)
			return null;
		return xrow.getCell(col);
	}

	Category getCategory(String string, ModelType type) {
		if (string == null)
			return null;
		String path = string.trim();
		if (path.isEmpty())
			return null;
		String[] elems = path.split("/");
		return Categories.findOrAdd(database, type, elems);
	}

}
