package org.openlca.io.xls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ProcessExport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private File file;
	private Process process;
	private CellStyle headerStyle;
	private CellStyle dateFormat;

	public ProcessExport(File file, Process process) {
		this.file = file;
		this.process = process;
	}

	@Override
	public void run() {
		try {
			Workbook workbook = new XSSFWorkbook();
			this.headerStyle = Excel.headerStyle(workbook);
			this.dateFormat = Excel.dateStyle(workbook);
			Sheet infoSheet = workbook.createSheet("General information");
			int row = 1;
			row = fillGeneralInfo(infoSheet, row);
			row = fillQuanRef(infoSheet, ++row);
			Excel.autoSize(infoSheet, 1);
		} catch (Exception e) {
			log.error("Excel export failed", e);
		}
	}

	private int fillGeneralInfo(Sheet sheet, int row) {
		header(sheet, row++, 1, "General information");
		cell(sheet, row++, "Name", process.getName());
		cell(sheet, row++, "Description", process.getDescription());
		List<String> path = new ArrayList<>();
		Category category = process.getCategory();
		while (category != null) {
			path.set(0, category.getName());
			category = category.getParentCategory();
		}
		cell(sheet, row++, "Category", Joiner.on('/').join(path));
		cell(sheet, row++, "Infractructure process",
				Boolean.toString(process.isInfrastructureProcess()));
		return row;
	}

	private int fillQuanRef(Sheet sheet, int row) {
		header(sheet, row++, 1, "Quantitative reference");
		Exchange qRef = process.getQuantitativeReference();
		if (qRef == null || qRef.getFlow() == null)
			return ++row;
		cell(sheet, row++, "Quantitative reference", qRef.getFlow().getName());
		return row;
	}

	private int fillTime(Sheet sheet, int row) {
		header(sheet, row++, 1, "Time");
		Cell startCell = cell(sheet, row++, "Start date");
		return row;
	}

	private void header(Sheet sheet, int row, int col, String value) {
		Excel.cell(sheet, row, col, value).setCellStyle(headerStyle);
	}

	private void cell(Sheet sheet, int row, String label, String value) {
		Excel.cell(sheet, row, 1, label);
		Excel.cell(sheet, row, 2, value);
	}

	private Cell cell(Sheet sheet, int row, String label) {
		Excel.cell(sheet, row, 1, label);
		return Excel.cell(sheet, row, 2);
	}

}
