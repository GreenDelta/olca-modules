package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

import java.util.HashSet;
import java.util.Set;

class UnitGroupSheet implements EntitySheet {

	private final ProcessWorkbook config;
	private final Set<UnitGroup> groups = new HashSet<>();

	UnitGroupSheet(ProcessWorkbook config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		Util.unitGroupsOf(entity, groups::add);
	}

	@Override
	public void flush() {
		if (groups.isEmpty())
			return;
		var sheet = config.workbook.createSheet("Unit groups");
		Excel.trackSize(sheet, 0, 7);
		writeHeader(sheet);
		int row = 0;
		for (var group : Util.sort(groups)) {
			row++;
			write(sheet, row, group);
		}
		Excel.autoSize(sheet, 0, 7);
	}

	private void writeHeader(Sheet sheet) {
		config.header(sheet, 0, 0, "UUID");
		config.header(sheet, 0, 1, "Name");
		config.header(sheet, 0, 2, "Description");
		config.header(sheet, 0, 3, "Category");
		config.header(sheet, 0, 4, "Reference unit");
		config.header(sheet, 0, 5, "Default flow property");
		config.header(sheet, 0, 6, "Version");
		config.header(sheet, 0, 7, "Last change");
	}

	private void write(Sheet sheet, int row, UnitGroup group) {
		Excel.cell(sheet, row, 0, group.refId);
		Excel.cell(sheet, row, 1, group.name);
		Excel.cell(sheet, row, 2, group.description);
		Excel.cell(sheet, row, 3, CategoryPath.getFull(group.category));
		if (group.referenceUnit != null) {
			Excel.cell(sheet, row, 4, group.referenceUnit.name);
		}
		if (group.defaultFlowProperty != null) {
			Excel.cell(sheet, row, 5, group.defaultFlowProperty.name);
		}
		Excel.cell(sheet, row, 6, Version.asString(group.version));
		config.date(sheet, row, 7, group.lastChange);
	}
}
