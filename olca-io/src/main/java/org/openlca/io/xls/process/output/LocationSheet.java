package org.openlca.io.xls.process.output;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.model.Location;
import org.openlca.core.model.RootEntity;
import org.openlca.io.xls.Excel;

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
		var sheet = wb.workbook.createSheet("Locations");
		Excel.trackSize(sheet, 0, 5);
		writeHeader(sheet);
		int row = 0;
		for (var location : Util.sort(locations)) {
			row++;
			write(sheet, row, location);
		}
		Excel.autoSize(sheet, 0, 5);
	}

	private void writeHeader(Sheet sheet) {
		wb.header(sheet, 0, 0, "UUID");
		wb.header(sheet, 0, 1, "Code");
		wb.header(sheet, 0, 2, "Name");
		wb.header(sheet, 0, 3, "Description");
		wb.header(sheet, 0, 4, "Latitude");
		wb.header(sheet, 0, 5, "Longitude");
	}

	private void write(Sheet sheet, int row, Location location) {
		Excel.cell(sheet, row, 0, location.refId);
		Excel.cell(sheet, row, 1, location.code);
		Excel.cell(sheet, row, 2, location.name);
		Excel.cell(sheet, row, 3, location.description);
		Excel.cell(sheet, row, 4, location.latitude);
		Excel.cell(sheet, row, 5, location.longitude);
	}

}
