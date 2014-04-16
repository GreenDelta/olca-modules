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

	private LocationSheet(Config config) {
		this.config = config;
		this.dao = new LocationDao(config.database);
	}

	public static void read(Config config) {
		new LocationSheet(config).read();
	}

	private void read() {
		try {
			log.trace("import locations");
			Sheet sheet = config.workbook.getSheet("Locations");
			int row = 1;
			while (true) {
				String uuid = config.getString(sheet, row, 0);
				if (uuid == null || uuid.trim().isEmpty())
					break;
				readLocation(uuid, row, sheet);
				row++;
			}
		} catch (Exception e) {
			log.error("failed to read locations", e);
		}
	}

	private void readLocation(String uuid, int row, Sheet sheet)
			throws Exception {
		String code = config.getString(sheet, row, 1);
		Location location = dao.getForRefId(uuid);
		if(location != null) {
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
