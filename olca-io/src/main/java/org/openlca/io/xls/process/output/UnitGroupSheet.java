package org.openlca.io.xls.process.output;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

import java.util.HashSet;
import java.util.Set;

class UnitGroupSheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<UnitGroup> groups = new HashSet<>();

	UnitGroupSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		Util.unitGroupsOf(entity, groups::add);
	}

	@Override
	public void flush() {
		if (groups.isEmpty())
			return;

		var cursor = wb.createCursor("Unit groups")
				.withColumnWidths(40, 40, 40);
		cursor.header(
				"UUID",
				"Name",
				"Category",
				"Description",
				"Reference unit",
				"Default flow property",
				"Version",
				"Last change"
		);

		for (var group : Util.sort(groups)) {
			cursor.next(row -> {
				Excel.cell(row, 0, group.refId);
				Excel.cell(row, 1, group.name);
				Excel.cell(row, 2, CategoryPath.getFull(group.category));
				Excel.cell(row, 3, group.description);
				if (group.referenceUnit != null) {
					Excel.cell(row, 4, group.referenceUnit.name);
				}
				if (group.defaultFlowProperty != null) {
					Excel.cell(row, 5, group.defaultFlowProperty.name);
				}
				Excel.cell(row, 6, Version.asString(group.version));
				wb.date(row, 7, group.lastChange);
			});
		}
	}
}
