package org.openlca.geo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.geo.geojson.Point;
import org.openlca.commons.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imports geographic information from a GeoJSON file. Meta-data are taken
 * from the properties of a feature. The import understands meta-data as
 * defined in the
 * <a href="https://geography.ecoinvent.org/">ecoinvent geographies</a>.
 */
public class GeoJsonImport implements Runnable {

	/**
	 * The possible import modes.
	 */
	public enum Mode {

		/**
		 * Only create new geographies that do not exist already.
		 */
		NEW_ONLY,

		/**
		 * Only update the geometries of geographies that already exist.
		 */
		UPDATE_ONLY,

		/**
		 * Create new and updated existing locations.
		 */
		NEW_AND_UPDATE;

		boolean canCreate() {
			return this == NEW_ONLY || this == NEW_AND_UPDATE;
		}

		boolean canUpdate() {
			return this == UPDATE_ONLY || this == NEW_AND_UPDATE;
		}
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final String[] codeFields = {
			"code",
			"isotwolettercode",
			"isothreelettercode",
			"shortname",
	};
	private final String[] nameFields = {"name", "shortname"};
	private final String[] idFields = {"uuid", "refId"};
	private final String[] categoryFields = {"collection", "category"};

	private final String[] mappingFields = {
			"uuid",
			"refId",
			"code",
			"isotwolettercode",
			"isothreelettercode",
			"name",
			"shortname",
	};

	private final Map<String, Location> byUUID = new HashMap<>();
	private final Map<String, Location> byCode = new HashMap<>();
	private final Map<String, Location> byName = new HashMap<>();
	private final List<Map<String, Location>> maps = List.of(
			byUUID, byCode, byName
	);

	private final File file;
	private final IDatabase db;
	private Mode mode = Mode.NEW_ONLY;

	public GeoJsonImport(File file, IDatabase db) {
		this.file = file;
		this.db = db;
	}

	public GeoJsonImport withMode(Mode mode) {
		if (mode != null) {
			this.mode = mode;
		}
		return this;
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
			for (var loc : db.getAll(Location.class)) {
				index(loc);
			}

			// create and/or update locations
			for (var feature : coll.features) {
				if (feature.geometry == null || feature.properties == null)
					continue;
				var loc = findExisting(feature);
				if (loc == null && mode.canCreate()) {
					create(feature);
				} else if (loc != null && loc.geodata == null && mode.canUpdate()) {
					update(loc, feature);
				}
			}
		} catch (Exception e) {
			log.error("Failed to import GeoJSON file {}", file, e);
		}
	}

	private void create(Feature feature) {

		// meta-data
		var code = anyStrOf(feature, codeFields);
		var name = anyStrOf(feature, nameFields);
		if (name == null) {
			if (code == null)
				return;
			name = code;
		}
		var loc = code != null
				? Location.of(name, code)
				: Location.of(name);
		var uuid = anyStrOf(feature, idFields);
		if (uuid != null) {
			loc.refId = uuid;
		}

		// category
		var collection = anyStrOf(feature, categoryFields);
		if (collection != null && collection.length() > 1) {
			var cat = collection.substring(0, 1).toUpperCase()
					+ collection.substring(1);
			loc.category = CategoryDao.sync(db, ModelType.LOCATION, cat);
		}

		// geo-data
		loc.geodata = GeoJSON.pack(FeatureCollection.of(feature.geometry));
		var center = centerOf(feature);
		if (center != null) {
			loc.longitude = center.x;
			loc.latitude = center.y;
		}

		index(db.insert(loc));
	}

	private void update(Location loc, Feature feature) {
		loc.geodata = GeoJSON.pack(FeatureCollection.of(feature.geometry));
		var center = centerOf(feature);
		if (center != null) {
			loc.longitude = center.x;
			loc.latitude = center.y;
		}
		db.update(loc);
	}

	private void index(Location loc) {
		if (loc == null)
			return;
		BiConsumer<String, Map<String, Location>> idx = (rawKey, map) -> {
			if (Strings.isBlank(rawKey))
				return;
			var key = rawKey.strip().toLowerCase();
			map.put(key, loc);
		};
		idx.accept(loc.refId, byUUID);
		idx.accept(loc.code, byCode);
		idx.accept(loc.name, byName);
	}

	private Location findExisting(Feature f) {
		if (f == null || f.geometry == null || f.properties == null)
			return null;
		// we try to match corresponding locations first by UUID,
		// then by location code, and finally by name
		for (var field : mappingFields) {
			var prop = f.properties.get(field);
			if (!(prop instanceof String s) || Strings.isBlank(s))
				continue;
			var key = s.strip().toLowerCase();
			for (var map : maps) {
				var loc = map.get(key);
				if (loc != null) {
					log.trace("identified location {} via attribute {}", loc, s);
					return loc;
				}
			}
		}
		return null;
	}

	private Point centerOf(Feature feature) {
		if (feature == null || feature.properties == null)
			return null;
		if (feature.properties.get("latitude") instanceof Number lat
				&& feature.properties.get("longitude") instanceof Number lon) {
			return new Point(lon.doubleValue(), lat.doubleValue());
		}
		return null;
	}

	private String anyStrOf(Feature feature, String[] props) {
		for (var prop : props) {
			var s = strOf(feature, prop);
			if (s != null)
				return s;
		}
		return null;
	}

	private String strOf(Feature feature, String property) {
		if (feature == null || feature.properties == null)
			return null;
		var obj = feature.properties.get(property);
		return obj instanceof String s && Strings.isNotBlank(s)
				? s.strip()
				: null;
	}
}
