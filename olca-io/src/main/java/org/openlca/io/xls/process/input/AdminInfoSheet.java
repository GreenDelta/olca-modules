package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AdminInfoSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final ProcessDocumentation doc;
	private final Sheet sheet;

	private AdminInfoSheet(Config config) {
		this.config = config;
		doc = config.process.documentation;
		sheet = config.workbook.getSheet("Administrative information");
	}

	public static void read(Config config) {
		new AdminInfoSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read administrative information");
			doc.intendedApplication = config.getString(sheet, 1, 1);
			doc.dataSetOwner = readActor(2);
			doc.dataGenerator = readActor(3);
			doc.dataDocumentor = readActor(4);
			doc.publication = readSource(5);
			doc.restrictions = config.getString(sheet, 6, 1);
			doc.project = config.getString(sheet, 7, 1);
			doc.creationDate = config.getDate(sheet, 8, 1);
			readCopyright();
		} catch (Exception e) {
			log.error("failed to read administrative information", e);
		}
	}

	private Actor readActor(int row) {
		String name = config.getString(sheet, row, 1);
		if (name == null) {
			return null;
		}
		String category = config.getString(sheet, row, 2);
		return config.refData.getActor(name, category);
	}

	private Source readSource(int row) {
		String name = config.getString(sheet, row, 1);
		if (name == null) {
			return null;
		}
		String category = config.getString(sheet, row, 2);
		return config.refData.getSource(name, category);
	}

	private void readCopyright() {
		try {
			Cell cell = config.getCell(sheet, 9, 1);
			if (cell != null && cell.getCellType() == CellType.BOOLEAN) {
				doc.copyright = cell.getBooleanCellValue();
			}
		} catch (Exception e) {
			log.error("failed to read copyright cell", e);
		}
	}
}
