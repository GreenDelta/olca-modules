package org.openlca.io.xls.process.input;

import org.apache.poi.ss.usermodel.Sheet;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationSheet {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final Config config;
	private final LocationDao dao;
	private final Sheet sheet;

	private LocationSheet(Config config) {
		this.config = config;
		dao = new LocationDao(config.database);
		sheet = config.workbook.getSheet("Locations");
	}

	public static void read(Config config) {
		new LocationSheet(config).read();
	}

	private void read() {
		if (sheet == null) {
			return;
		}
		try {
			log.trace("import locations");
			int row = 1;
			while (true) {
				String uuid = config.getString(sheet, row, 0);
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
		String code = config.getString(sheet, row, 1);
		Location location = dao.getForRefId(uuid);
		if (location != null) {
			config.refData.putLocation(code, location);
			return;
		}
		location = new Location();
		location.setRefId(uuid);
		location.setCode(code);
		location.setName(config.getString(sheet, row, 2));
		location.setDescription(config.getString(sheet, row, 3));
		location.setLatitude(config.getDouble(sheet, row, 4));
		location.setLongitude(config.getDouble(sheet, row, 5));
		location = dao.insert(location);
		config.refData.putLocation(code, location);
	}

}
