package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class AdminInfoSheet {

	private final ProcessWorkbook wb;
	private final Sheet sheet;
	private int row = 0;

	AdminInfoSheet(ProcessWorkbook wb) {
		this.wb = wb;
		sheet = wb.workbook.createSheet("Administrative information");
	}

	void write() {
		var doc = wb.process.documentation;
		if (doc == null)
			return;
		Excel.trackSize(sheet, 0, 0);
		wb.header(sheet, row++, 0, "Administrative information");

		pair("Intended application", doc.intendedApplication);
		writeEntity("Data set owner", doc.dataSetOwner);
		writeEntity("Data set generator", doc.dataGenerator);
		writeEntity("Data set documentor", doc.dataDocumentor);
		writeEntity("Publication", doc.publication);

		pair("Access and use restrictions", doc.restrictions);
		pair("Project", doc.project);

		Excel.cell(sheet, row, 0, "Creation date");
		wb.date(sheet, row++, 1, doc.creationDate);

		Excel.cell(sheet, row, 0, "Copyright");
		Excel.cell(sheet, row++, 1, doc.copyright);

		Excel.autoSize(sheet, 0, 0);
		sheet.setColumnWidth(1, 100 * 256);
	}

	private void writeEntity(String header, RootEntity entity) {
		if (entity == null) {
			Excel.cell(sheet, row++, 0, header);
			return;
		}
		wb.put(entity);
		Excel.cell(sheet, row, 0, header);
		Excel.cell(sheet, row, 1, entity.name);
		Excel.cell(sheet, row++, 2, CategoryPath.getFull(entity.category));
	}

	private void pair(String header, String value) {
		wb.pair(sheet, row++, header, value);
	}
}
