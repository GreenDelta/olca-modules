package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AdminInfoSheet {

	public static void read(final Config config) {
		new AdminInfoSheet(config).read();
	}

	private final Config config;
	private final ProcessDocumentation doc;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Sheet sheet;

	private AdminInfoSheet(final Config config) {
		this.config = config;
		doc = config.process.getDocumentation();
		sheet = config.workbook.getSheet("Administrative information");
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("read administrative information");
			doc.setIntendedApplication(config.getString(sheet, 1, 1));
			doc.setDataSetOwner(readActor(2));
			doc.setDataGenerator(readActor(3));
			doc.setDataDocumentor(readActor(4));
			doc.setPublication(readSource(5));
			doc.setRestrictions(config.getString(sheet, 6, 1));
			doc.setProject(config.getString(sheet, 7, 1));
			doc.setCreationDate(config.getDate(sheet, 8, 1));
			readCopyright();
		} catch (final Exception e) {
			log.error("failed to read administrative information", e);
		}
	}

	private Actor readActor(final int row) {
		final String name = config.getString(sheet, row, 1);
		if (name == null) {
			return null;
		}
		final String category = config.getString(sheet, row, 2);
		return config.refData.getActor(name, category);
	}

	private void readCopyright() {
		try {
			final Cell cell = config.getCell(sheet, 9, 1);
			if (cell != null && cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
				doc.setCopyright(cell.getBooleanCellValue());
			}
		} catch (final Exception e) {
			log.error("failed to read copyright cell", e);
		}
	}

	private Source readSource(final int row) {
		final String name = config.getString(sheet, row, 1);
		if (name == null) {
			return null;
		}
		final String category = config.getString(sheet, row, 2);
		return config.refData.getSource(name, category);
	}
}
