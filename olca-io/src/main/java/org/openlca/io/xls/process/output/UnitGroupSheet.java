package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

class UnitGroupSheet {

	private final Config config;
	private final Sheet sheet;

	private int row = 0;

	private UnitGroupSheet(Config config) {
		this.config = config;
		sheet = config.workbook.createSheet("Unit groups");
	}

	public static void write(Config config) {
		new UnitGroupSheet(config).write();
	}

	private void write() {
		Excel.trackSize(sheet, 0, 7);
		writeHeader();
		var groups = new UnitGroupDao(config.database).getAll();
		groups.sort(new EntitySorter());
		for (UnitGroup group : groups) {
			row++;
			write(group);
		}
		Excel.autoSize(sheet, 0, 7);
	}

	private void writeHeader() {
		config.header(sheet, row, 0, "UUID");
		config.header(sheet, row, 1, "Name");
		config.header(sheet, row, 2, "Description");
		config.header(sheet, row, 3, "Category");
		config.header(sheet, row, 4, "Reference unit");
		config.header(sheet, row, 5, "Default flow property");
		config.header(sheet, row, 6, "Version");
		config.header(sheet, row, 7, "Last change");
	}

	private void write(UnitGroup group) {
		Excel.cell(sheet, row, 0, group.refId);
		Excel.cell(sheet, row, 1, group.name);
		Excel.cell(sheet, row, 2, group.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(group.category));
		if (group.referenceUnit != null)
			Excel.cell(sheet, row, 4, group.referenceUnit.name);
		if (group.defaultFlowProperty != null)
			Excel.cell(sheet, row, 5, group.defaultFlowProperty.name);
		Excel.cell(sheet, row, 6, Version.asString(group.version));
		config.date(sheet, row, 7, group.lastChange);
	}
}
