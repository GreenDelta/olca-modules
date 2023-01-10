package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.util.Strings;

class AdminInfoSheet {

	private final ProcessWorkbook wb;
	private final ProcessDocumentation doc;
	private final Sheet sheet;

	private AdminInfoSheet(ProcessWorkbook wb) {
		this.wb = wb;
		doc = wb.process.documentation;
		sheet = wb.getSheet("Administrative information");
	}

	public static void read(ProcessWorkbook wb) {
		new AdminInfoSheet(wb).read();
	}

	private void read() {
		if (sheet == null)
			return;
		doc.intendedApplication = wb.getString(sheet, 1, 1);
		doc.dataSetOwner = readActor(2);
		doc.dataGenerator = readActor(3);
		doc.dataDocumentor = readActor(4);
		doc.publication = readSource(5);
		doc.restrictions = wb.getString(sheet, 6, 1);
		doc.project = wb.getString(sheet, 7, 1);
		doc.creationDate = wb.getDate(sheet, 8, 1);
		readCopyright();
	}

	private Actor readActor(int row) {
		var name = wb.getString(sheet, row, 1);
		return Strings.notEmpty(name)
				? wb.index.get(Actor.class, name)
				: null;
	}

	private Source readSource(int row) {
		var name = wb.getString(sheet, row, 1);
		return Strings.notEmpty(name)
				? wb.index.get(Source.class, name)
				: null;
	}

	private void readCopyright() {
		try {
			Cell cell = wb.getCell(sheet, 9, 1);
			if (cell != null && cell.getCellType() == CellType.BOOLEAN) {
				doc.copyright = cell.getBooleanCellValue();
			}
		} catch (Exception e) {
			wb.log.error("failed to read copyright cell", e);
		}
	}
}
