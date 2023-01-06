package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

import java.util.HashSet;
import java.util.Set;

class SourceSheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<Source> sources = new HashSet<>();

	SourceSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Source source) {
			sources.add(source);
		}
	}

	@Override
	public void flush() {
		if (sources.isEmpty())
			return;
		var sheet = wb.workbook.createSheet("Sources");
		Excel.trackSize(sheet, 0, 5);
		writeHeader(sheet);
		int row = 0;
		for (var source : Util.sort(sources)) {
			row++;
			write(sheet, row, source);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader(Sheet sheet) {
		wb.header(sheet, 0, 0, "UUID");
		wb.header(sheet, 0, 1, "Name");
		wb.header(sheet, 0, 2, "Description");
		wb.header(sheet, 0, 3, "Category");
		wb.header(sheet, 0, 4, "Version");
		wb.header(sheet, 0, 5, "Last change");
		wb.header(sheet, 0, 6, "URL");
		wb.header(sheet, 0, 7, "Text reference");
		wb.header(sheet, 0, 8, "Year");
	}

	private void write(Sheet sheet, int row, Source source) {
		Excel.cell(sheet, row, 0, source.refId);
		Excel.cell(sheet, row, 1, source.name);
		Excel.cell(sheet, row, 2, source.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(source.category));
		Excel.cell(sheet, row, 4, Version.asString(source.version));
		wb.date(sheet, row, 5, source.lastChange);
		Excel.cell(sheet, row, 6, source.url);
		Excel.cell(sheet, row, 7, source.textReference);
		if (source.year != null) {
			Excel.cell(sheet, row, 8)
					.ifPresent(c -> c.setCellValue(source.year));
		}
	}

}
