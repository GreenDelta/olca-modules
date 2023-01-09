package org.openlca.io.xls.process.output;

import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

import java.util.HashSet;
import java.util.Set;

class LocationSheet implements EntitySheet {

	private final ProcessWorkbook wb;
	private final Set<Location> locations = new HashSet<>();

	LocationSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Location location) {
			locations.add(location);
		}
	}

	@Override
	public void flush() {
		if (locations.isEmpty())
			return;
		var cursor = wb.createCursor("Locations")
				.withColumnWidths(5, 25);
		cursor.header(
				Field.UUID, // 0
				Field.CODE, // 1
				Field.NAME, // 2
				Field.CATEGORY, // 3
				Field.DESCRIPTION, // 4
				Field.LATITUDE, // 5
				Field.LONGITUDE, // 6
				Field.LAST_CHANGE, // 7
				Field.VERSION // 8
		);

		for (var location : Util.sort(locations)) {
			cursor.next(row -> {
				Excel.cell(row, 0, location.refId);
				Excel.cell(row, 1, location.code);
				Excel.cell(row, 2, location.name);
				Excel.cell(row, 3, CategoryPath.getFull(location.category));
				Excel.cell(row, 4, location.description);
				Excel.cell(row, 5, location.latitude);
				Excel.cell(row, 6, location.longitude);
				wb.date(row, 7, location.lastChange);
				Excel.cell(row, 8, new Version(location.version).toString());
			});
		}
	}
}
