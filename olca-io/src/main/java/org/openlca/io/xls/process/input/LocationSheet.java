package org.openlca.io.xls.process.input;

import org.openlca.core.model.Location;

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
		if (sheet == null) {
			return;
		}


		try {
			log.trace("import locations");
			int row = 1;
			while (true) {
				String uuid = wb.getString(sheet, row, 0);
				if (uuid == null || uuid.trim().isEmpty()) {
					break;
				}
				readLocation(uuid, row);
				row++;
			}
		} catch (Exception e) {
			log.error("failed to read locations", e);
		}
	}

	private void readLocation(String uuid, int row) throws Exception {
		String code = wb.getString(sheet, row, 1);
		Location location = dao.getForRefId(uuid);
		if (location != null) {
			wb.refData.putLocation(code, location);
			return;
		}
		location = new Location();
		location.refId = uuid;
		location.code = code;
		location.name = wb.getString(sheet, row, 2);
		location.description = wb.getString(sheet, row, 3);
		location.latitude = wb.getDouble(sheet, row, 4);
		location.longitude = wb.getDouble(sheet, row, 5);
		location = dao.insert(location);
		wb.refData.putLocation(code, location);
	}

}
