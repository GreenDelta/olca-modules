package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.io.xls.process.Field;

class LocationSheet {

	private final ProcessWorkbook wb;

	private LocationSheet(ProcessWorkbook wb) {
		this.wb = wb;
	}

	public static void read(ProcessWorkbook config) {
		new LocationSheet(config).read();
	}

	private void read() {
		var sheet = wb.getSheet("Locations");
		if (sheet == null)
			return;
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return;
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var refId = fields.str(row, Field.UUID);
			wb.index.sync(Location.class, refId, () -> create(row, fields));
		});
	}

	private Location create(Row row, FieldMap fields) {
		var location = new Location();
		Util.mapBase(row, fields, location);
		location.category = fields.category(row, ModelType.LOCATION, wb.db);
		location.code = fields.str(row, Field.CODE);
		location.latitude = fields.num(row, Field.LATITUDE);
		location.longitude = fields.num(row, Field.LONGITUDE);
		return location;
	}

}
