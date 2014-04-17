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

	Config(final Workbook workbook, final IDatabase database,
			final Process process) {
		this.workbook = workbook;
		this.database = database;
		this.process = process;
		this.refData = new RefData();
	}

	Category getCategory(final String string, final ModelType type) {
		if (string == null) {
			return null;
		}
		final String path = string.trim();
		if (path.isEmpty()) {
			return null;
		}
		final String[] elems = path.split("/");
		return Categories.findOrAdd(database, type, elems);
	}

	Cell getCell(final Sheet sheet, final int row, final int col) {
		if (sheet == null) {
			return null;
		}
		final Row xrow = sheet.getRow(row);
		if (xrow == null) {
			return null;
		}
		return xrow.getCell(col);
	}

	Date getDate(final Sheet sheet, final int row, final int col) {
		final Cell cell = getCell(sheet, row, col);
		if (cell == null) {
			return null;
		}
		try {
			return cell.getDateCellValue();
		} catch (final Exception e) {
			return null;
		}
	}

	double getDouble(final Sheet sheet, final int row, final int col) {
		final Cell cell = getCell(sheet, row, col);
		if (cell == null) {
			return 0;
		}
		try {
			return cell.getNumericCellValue();
		} catch (final Exception e) {
			return 0;
		}
	}

	String getString(final Sheet sheet, final int row, final int col) {
		final Cell cell = getCell(sheet, row, col);
		if (cell == null) {
			return null;
		}
		try {
			final String s = cell.getStringCellValue();
			return s != null ? s.trim() : null;
		} catch (final Exception e) {
			// we do not use the cell type check but try it the hard way
			// instead because the cell type may be of formula type which
			// will return a string
			return null;
		}
	}

	Uncertainty getUncertainty(final Sheet sheet, final int row, final int col) {
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

	private Uncertainty logNormal(final Sheet sheet, final int row,
			final int col) {
		final double gmean = getDouble(sheet, row, col + 1);
		final double gsd = getDouble(sheet, row, col + 2);
		return Uncertainty.logNormal(gmean, gsd);
	}

	private Uncertainty normal(final Sheet sheet, final int row, final int col) {
		final double mean = getDouble(sheet, row, col + 1);
		final double sd = getDouble(sheet, row, col + 2);
		return Uncertainty.normal(mean, sd);
	}

	private Uncertainty triangular(final Sheet sheet, final int row,
			final int col) {
		final double min = getDouble(sheet, row, col + 3);
		final double mode = getDouble(sheet, row, col + 1);
		final double max = getDouble(sheet, row, col + 4);
		return Uncertainty.triangle(min, mode, max);
	}

	private Uncertainty uniform(final Sheet sheet, final int row, final int col) {
		final double min = getDouble(sheet, row, col + 3);
		final double max = getDouble(sheet, row, col + 4);
		return Uncertainty.uniform(min, max);
	}

}
