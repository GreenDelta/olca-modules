package org.openlca.io.xls.process.output;

import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;

import java.util.HashSet;
import java.util.Set;

class ProviderSheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<Process> providers = new HashSet<>();

	ProviderSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}


	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Process p) {
			providers.add(p);
		}
	}

	@Override
	public void flush() {
		if (providers.isEmpty())
			return;
		var cursor = wb.createCursor("Providers");
		cursor.header(
				"UUID",
				"Name",
				"Category",
				"Location");
		for (var p : providers) {
			cursor.next(row -> {
				Excel.cell(row, 0, p.refId);
				Excel.cell(row, 1, p.name);
				Excel.cell(row, 2, CategoryPath.getFull(p.category));
				if (p.location != null) {
					Excel.cell(row, 3, p.location.name);
				}
			});
		}
	}
}
