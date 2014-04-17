package org.openlca.io.xls.process.input;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SourceSheet {

	public static void read(final Config config) {
		new SourceSheet(config).read();
	}

	private final Config config;
	private final SourceDao dao;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Sheet sheet;

	private SourceSheet(final Config config) {
		this.config = config;
		sheet = config.workbook.getSheet("Sources");
		dao = new SourceDao(config.database);
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("import sources");
			int row = 1;
			while (true) {
				final String uuid = config.getString(sheet, row, 0);
				if (uuid == null || uuid.trim().isEmpty()) {
					break;
				}
				readSource(uuid, row);
				row++;
			}
		} catch (final Exception e) {
			log.error("failed to read source sheet", e);
		}
	}

	private void readSource(final String uuid, final int row) {
		final String name = config.getString(sheet, row, 1);
		final String category = config.getString(sheet, row, 3);
		Source source = dao.getForRefId(uuid);
		if (source != null) {
			config.refData.putSource(name, category, source);
			return;
		}
		source = new Source();
		source.setRefId(uuid);
		source.setName(name);
		source.setDescription(config.getString(sheet, row, 2));
		source.setCategory(config.getCategory(category, ModelType.SOURCE));
		setAttributes(row, source);
		source = dao.insert(source);
		config.refData.putSource(name, category, source);
	}

	private void setAttributes(final int row, final Source source) {
		final String version = config.getString(sheet, row, 4);
		source.setVersion(Version.fromString(version).getValue());
		final Date lastChange = config.getDate(sheet, row, 5);
		if (lastChange != null) {
			source.setLastChange(lastChange.getTime());
		}
		source.setDoi(config.getString(sheet, row, 6));
		source.setTextReference(config.getString(sheet, row, 7));
		final Cell yearCell = config.getCell(sheet, row, 8);
		if (yearCell != null
				&& yearCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			final double y = yearCell.getNumericCellValue();
			source.setYear((short) y);
		}
	}
}
