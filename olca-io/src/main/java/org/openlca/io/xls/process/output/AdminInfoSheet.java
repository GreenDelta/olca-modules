package org.openlca.io.xls.process.output;

import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class AdminInfoSheet {

	private ProcessDocumentation doc;
	private Config config;
	private Sheet sheet;
	private int row = 0;

	private AdminInfoSheet(Config config) {
		this.config = config;
		doc = config.process.getDocumentation();
		sheet = config.workbook.createSheet("Administrative information");
	}

	public static void write(Config config) {
		new AdminInfoSheet(config).write();
	}

	private void write() {
		config.header(sheet, row++, 0, "Administrative information");
		pair("Intended application", doc.getIntendedApplication());
		pair("Data set owner", doc.getDataSetOwner());
		pair("Data set generator", doc.getDataGenerator());
		pair("Data set documentor", doc.getDataDocumentor());
		pair("Publication", doc.getPublication());
		pair("Access and use restrictions", doc.getRestrictions());
		pair("Project", doc.getProject());
		pair("Creation date", doc.getCreationDate());
		Excel.cell(sheet, row, 0, "Copyright");
		Excel.cell(sheet, row++, 1).setCellValue(doc.isCopyright());
		Excel.autoSize(sheet, 0);
		sheet.setColumnWidth(1, 100 * 256);
	}

	private void pair(String header, CategorizedEntity entity) {
		if (entity == null) {
			Excel.cell(sheet, row++, 0, header);
			return;
		}
		Excel.cell(sheet, row, 0, header);
		Excel.cell(sheet, row, 1, entity.getName());
		Excel.cell(sheet, row++, 2, CategoryPath.getFull(entity.getCategory()));
	}

	private void pair(String header, String value) {
		config.pair(sheet, row++, header, value);
	}

	private void pair(String header, Date value) {
		Excel.cell(sheet, row, 0, header);
		config.date(sheet, row++, 1, value);
	}
}
