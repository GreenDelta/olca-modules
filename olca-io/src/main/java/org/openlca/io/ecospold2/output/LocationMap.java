package org.openlca.io.ecospold2.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Location;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.Geography;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;

import java.util.HashMap;
import java.util.List;

class LocationMap {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;
	private final HashMap<String, ExportRecord> map = new HashMap<>();

	public LocationMap(IDatabase database) {
		this.database = database;
		initMap();
	}

	private void initMap() {
		try {
			List<List<Object>> rows = Maps.readAll(Maps.ES2_LOCATION_EXPORT,
					database, new CellProcessor[] { null, null, null, null });
			for (List<Object> row : rows) {
				String refId = Maps.getString(row, 0);
				ExportRecord record = new ExportRecord();
				record.id = Maps.getString(row, 2);
				record.code = Maps.getString(row, 3);
				map.put(refId, record);
			}
		} catch (Exception e) {
			log.error("failed to initialize location export map", e);
		}
	}

	public void apply(org.openlca.core.model.Process process, DataSet dataSet) {
		Geography geography = new Geography();
		dataSet.setGeography(geography);
		if (process.getDocumentation() != null)
			geography.setComment(process.getDocumentation().getGeography());
		if (process.getLocation() == null)
			setDefaultLocation(geography);
		else
			tryMapLocation(process.getLocation(), geography);
	}

	private void tryMapLocation(Location location, Geography geography) {
		ExportRecord record = map.get(location.getRefId());
		if (record == null) {
			log.warn("location {} is not a valid EcoSpold 2 location; set "
					+ "default location to GLO");
			setDefaultLocation(geography);
			return;
		}
		log.trace("mapped location {} to {}", location, record);
		geography.setId(record.id);
		geography.setShortName(record.code);
	}

	private void setDefaultLocation(Geography geography) {
		if (geography == null)
			return;
		geography.setId("34dbbff8-88ce-11de-ad60-0019e336be3a");
		geography.setShortName("GLO");
	}

	private class ExportRecord {

		String id;
		String code;

		@Override
		public String toString() {
			return code + " [" + id + "]";
		}
	}
}
