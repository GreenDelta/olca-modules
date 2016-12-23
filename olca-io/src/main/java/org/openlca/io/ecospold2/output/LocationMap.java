package org.openlca.io.ecospold2.output;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Location;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;

import spold2.ActivityDescription;
import spold2.DataSet;
import spold2.Geography;
import spold2.RichText;

class LocationMap {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final HashMap<String, ExportRecord> map = new HashMap<>();

	public LocationMap(IDatabase database) {
		initMap(database);
	}

	private void initMap(IDatabase database) {
		try {
			CellProcessor[] processors = { null, null, null, null };
			List<List<Object>> rows = Maps.readAll(Maps.ES2_LOCATION_EXPORT,
					database, processors);
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

	public void apply(org.openlca.core.model.Process p, DataSet ds) {
		if (ds.description == null)
			ds.description = new ActivityDescription();
		Geography geo = new Geography();
		ds.description.geography = geo;
		if (p.getDocumentation() != null)
			geo.comment = RichText.of(p.getDocumentation().getGeography());
		if (p.getLocation() == null)
			setDefaultLocation(geo);
		else
			tryMapLocation(p.getLocation(), geo);
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
		geography.id = record.id;
		geography.shortName = record.code;
	}

	private void setDefaultLocation(Geography geography) {
		if (geography == null)
			return;
		geography.id = "34dbbff8-88ce-11de-ad60-0019e336be3a";
		geography.shortName = "GLO";
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
