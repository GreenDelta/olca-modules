package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class SourceSheet {

	private final Config config;
	private final Sheet sheet;

	private int row = 0;

	private SourceSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Sources");
	}

	public static void write(Config config) {
		new SourceSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 5);
		writeHeader();
		var sources = new SourceDao(config.database).getAll();
		sources.sort(new EntitySorter());
		for (Source source : sources) {
			row++;
			write(source);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader() {
		config.header(sheet, row, 0, "UUID");
		config.header(sheet, row, 1, "Name");
		config.header(sheet, row, 2, "Description");
		config.header(sheet, row, 3, "Category");
		config.header(sheet, row, 4, "Version");
		config.header(sheet, row, 5, "Last change");
		config.header(sheet, row, 6, "URL");
		config.header(sheet, row, 7, "Text reference");
		config.header(sheet, row, 8, "Year");
	}

	private void write(Source source) {
		Excel.cell(sheet, row, 0, source.refId);
		Excel.cell(sheet, row, 1, source.name);
		Excel.cell(sheet, row, 2, source.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(source.category));
		Excel.cell(sheet, row, 4, Version.asString(source.version));
		config.date(sheet, row, 5, source.lastChange);
		Excel.cell(sheet, row, 6, source.url);
		Excel.cell(sheet, row, 7, source.textReference);
		if (source.year != null) {
			Excel.cell(sheet, row, 8)
					.ifPresent(c -> c.setCellValue(source.year));
		}
	}

}
