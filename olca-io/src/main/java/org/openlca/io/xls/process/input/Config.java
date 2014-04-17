package org.openlca.io.xls.process.input;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.io.Categories;

class Config {

	final IDatabase database;
	final Process process;
	final RefData refData;
	final Workbook workbook;

	Config(Workbook workbook, IDatabase database, Process process) {
		this.workbook = workbook;
		this.database = database;
		this.process = process;
		this.refData = new RefData();
	}

	Category getCategory(String string, ModelType type) {
		if (string == null) {
			return null;
		}
		String path = string.trim();
		if (path.isEmpty()) {
			return null;
		}
		String[] elems = path.split("/");
		return Categories.findOrAdd(database, type, elems);
	}

	Cell getCell(Sheet sheet, int row, int col) {
		if (sheet == null) {
			return null;
		}
		Row xrow = sheet.getRow(row);
		if (xrow == null) {
			return null;
		}
		return xrow.getCell(col);
	}

	Date getDate(Sheet sheet, int row, int col) {
		Cell cell = getCell(sheet, row, col);
		if (cell == null) {
			return null;
		}
		try {
			return cell.getDateCellValue();
		} catch (Exception e) {
			return null;
		}
	}

	double getDouble(Sheet sheet, int row, int col) {
		Cell cell = getCell(sheet, row, col);
		if (cell == null) {
			return 0;
		}
		try {
			return cell.getNumericCellValue();
		} catch (Exception e) {
			return 0;
		}
	}

	String getString(Sheet sheet, int row, int col) {
		Cell cell = getCell(sheet, row, col);
		if (cell == null) {
			return null;
		}
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

	Uncertainty getUncertainty(Sheet sheet, int row, int col) {
		String type = getString(sheet, row, col);
		if (type == null) {
			return null;
		}
		type = type.trim().toLowerCase();
		switch (type) {
		case "log-normal":
			return logNormal(sheet, row, col);
		case "normal":
			return normal(sheet, row, col);
		case "triangular":
			return triangular(sheet, row, col);
		case "uniform":
			return uniform(sheet, row, col);
		default:
			return null;
		}
	}

	private Uncertainty logNormal(Sheet sheet, int row, int col) {
		double gmean = getDouble(sheet, row, col + 1);
		double gsd = getDouble(sheet, row, col + 2);
		return Uncertainty.logNormal(gmean, gsd);
	}

	private Uncertainty normal(Sheet sheet, int row, int col) {
		double mean = getDouble(sheet, row, col + 1);
		double sd = getDouble(sheet, row, col + 2);
		return Uncertainty.normal(mean, sd);
	}

	private Uncertainty triangular(Sheet sheet, int row, int col) {
		double min = getDouble(sheet, row, col + 3);
		double mode = getDouble(sheet, row, col + 1);
		double max = getDouble(sheet, row, col + 4);
		return Uncertainty.triangle(min, mode, max);
	}

	private Uncertainty uniform(Sheet sheet, int row, int col) {
		double min = getDouble(sheet, row, col + 3);
		double max = getDouble(sheet, row, col + 4);
		return Uncertainty.uniform(min, max);
	}

}
