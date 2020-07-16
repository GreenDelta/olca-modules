package org.openlca.geo;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.geo.geojson.ProtoPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GeoJSON import tries to identify the corresponding locations in the
 * database via the feature attributes in a feature collection and updates
 * the corresponding geo data of that location if a corresponding feature
 * was found. This is currently mainly used for importing the geographic
 * information for ecoinvent available from https://geography.ecoinvent.org/.
 */
public class GeoJsonImport implements Runnable {

	private final File file;
	private final IDatabase db;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, Location> byUUID = new HashMap<>();
	private final Map<String, Location> byCode = new HashMap<>();
	private final Map<String, Location> byName = new HashMap<>();

	public GeoJsonImport(File file, IDatabase db) {
		this.file = file;
		this.db = db;
	}

	@Override
	public void run() {
		try {

			// parse GeoJSON
			log.trace("parse GeoJSON file {}", file);
			FeatureCollection coll = GeoJSON.read(file);
			if (coll == null || coll.features.isEmpty())
				return;

			// index locations
			LocationDao dao = new LocationDao(db);
			indexLocations(dao);

			// match and update the locations
			for (Feature f : coll.features) {
				if (f.geometry == null || f.properties == null)
					continue;
				Location loc = findMatch(f);
				if (loc == null)
					continue;
				loc.geodata = ProtoPack.packgz(FeatureCollection.of(f.geometry));
				dao.update(loc);
			}
		} catch (Exception e) {
			log.error("Failed to import GeoJSON file " + file, e);
		}
	}

	private void indexLocations(LocationDao dao) {
		for (Location loc : dao.getAll()) {
			if (loc.refId != null) {
				byUUID.put(loc.refId, loc);
			}
			if (loc.code != null) {
				byCode.put(loc.code, loc);
			}
			if (loc.name != null) {
				byName.put(loc.name, loc);
			}
		}
	}

	private Location findMatch(Feature f) {
		if (f == null || f.geometry == null || f.properties == null)
			return null;
		// we try to match corresponding locations
		// first by UUID, then by location code,
		// and finally by name
		for (Map<String, Location> map : Arrays.asList(byUUID, byCode, byName)) {
			for (Object prop : f.properties.values()) {
				if (!(prop instanceof String))
					continue;
				String s = (String) prop;
				Location loc = map.get(s);
				if (loc != null && loc.geodata == null) {
					log.trace("identified location {} via attribute {}", loc, s);
					return loc;
				}
			}
		}
		return null;
	}
}
