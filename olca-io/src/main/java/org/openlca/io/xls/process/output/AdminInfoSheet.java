package org.openlca.io.xls.process.output;

import java.util.Date;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class AdminInfoSheet {

	private final ProcessDocumentation doc;
	private final Config config;
	private final Sheet sheet;
	private int row = 0;

	private AdminInfoSheet(Config config) {
		this.config = config;
		doc = config.process.documentation;
		sheet = config.workbook.createSheet("Administrative information");
	}

	public static void write(Config config) {
		new AdminInfoSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 0);
		config.header(sheet, row++, 0, "Administrative information");
		pair("Intended application", doc.intendedApplication);
		pair("Data set owner", doc.dataSetOwner);
		pair("Data set generator", doc.dataGenerator);
		pair("Data set documentor", doc.dataDocumentor);
		pair("Publication", doc.publication);
		pair("Access and use restrictions", doc.restrictions);
		pair("Project", doc.project);
		pair("Creation date", doc.creationDate);
		Excel.cell(sheet, row, 0, "Copyright");
		Excel.cell(sheet, row++, 1, doc.copyright);
		Excel.autoSize(sheet, 0, 0);
		sheet.setColumnWidth(1, 100 * 256);
	}

	private void pair(String header, RootEntity entity) {
		if (entity == null) {
			Excel.cell(sheet, row++, 0, header);
			return;
		}
		Excel.cell(sheet, row, 0, header);
		Excel.cell(sheet, row, 1, entity.name);
		Excel.cell(sheet, row++, 2, CategoryPath.getFull(entity.category));
	}

	private void pair(String header, String value) {
		config.pair(sheet, row++, header, value);
	}

	private void pair(String header, Date value) {
		Excel.cell(sheet, row, 0, header);
		config.date(sheet, row++, 1, value);
	}
}
