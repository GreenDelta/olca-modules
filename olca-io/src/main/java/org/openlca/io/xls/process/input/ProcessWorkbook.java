package org.openlca.io.xls.process.input;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Uncertainty;
import org.openlca.io.Categories;

class ProcessWorkbook {

	final IDatabase db;
	final Process process;
	final EntityIndex index;
	final ImportLog log;

	private final Workbook wb;

	private ProcessWorkbook(ExcelImport config, Workbook wb, Process process) {
		this.wb = wb;
		this.db = config.db();
		this.log = config.log();
		this.process = process;
		this.index = new EntityIndex(db, config.log());
	}

	static Process read(File file, ExcelImport imp) {
		try (var fis = new FileInputStream(file)) {
			var wb = WorkbookFactory.create(fis);
			var process = new Process();
			process.documentation = new ProcessDocumentation();
			new ProcessWorkbook(imp, wb, process).readSheets();
			return process;
		} catch (Exception e) {
			imp.log().error("failed to import file", e);
			return null;
		}
	}

	Sheet getSheet(String name) {
		return wb.getSheet(name);
	}

	private void readSheets() {
		// reference data
		Locations.sync(this);
		Actors.sync(this);
		Sources.sync(this);
		Units.sync(this);
		Flows.sync(this);

		// process sheets
		IOSheet.readInputs(this);
		IOSheet.readOutputs(this);
		InfoSheet.read(this); // after exchanges! find qRef
		AdminInfoSheet.read(this);
		ModelingSheet.read(this);
		ParameterSheet.read(this);
		AllocationSheet.read(this);
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
		return Categories.findOrAdd(db, type, elems);
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
		return switch (type) {
			case "log-normal" -> logNormal(sheet, row, col);
			case "normal" -> normal(sheet, row, col);
			case "triangular" -> triangular(sheet, row, col);
			case "uniform" -> uniform(sheet, row, col);
			default -> null;
		};
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
