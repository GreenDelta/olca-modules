package org.openlca.io.xls.process.output;

import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class SourceSheet {

	private Config config;
	private Sheet sheet;
	private int row = 0;

	private SourceSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Sources");
	}

	public static void write(Config config) {
		new SourceSheet(config).write();
	}

	private void write() {
		writeHeader();
		SourceDao dao = new SourceDao(config.database);
		List<Source> sources = dao.getAll();
		Collections.sort(sources, new EntitySorter());
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
		Excel.cell(sheet, row, 0, source.getRefId());
		Excel.cell(sheet, row, 1, source.getName());
		Excel.cell(sheet, row, 2, source.getDescription());
		Excel.cell(sheet, row, 3, CategoryPath.getFull(source.getCategory()));
		Excel.cell(sheet, row, 4, Version.asString(source.getVersion()));
		config.date(sheet, row, 5, source.getLastChange());
		Excel.cell(sheet, row, 6, source.getUrl());
		Excel.cell(sheet, row, 7, source.getTextReference());
		if (source.getYear() != null) {
			Excel.cell(sheet, row, 8).setCellValue(source.getYear());
		}
	}

}
