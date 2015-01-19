package org.openlca.io.ilcd.input;

import org.openlca.core.database.BaseEntityDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Location;
import org.openlca.io.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Locations {

	private Locations() {
	}

	/**
	 * Returns the location with the given code from the database or null if it
	 * not exists in the database.
	 */
	public static Location get(String code, IDatabase database) {
		if (code == null || database == null)
			return null;
		try {
			String refId = KeyGen.get(code);
			BaseEntityDao<Location> dao = new BaseEntityDao<>(
					Location.class, database);
			return dao.getForRefId(refId);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Locations.class);
			log.error("failed to get location: " + code, e);
			return null;
		}
	}

	/**
	 * Returns the location with the given code from the database or creates a
	 * new one if it not yet exists.
	 */
	public static Location getOrCreate(String code, IDatabase database) {
		if (code == null || database == null)
			return null;
		Location location = get(code, database);
		if (location != null)
			return location;
		try {
			String refId = KeyGen.get(code);
			BaseEntityDao<Location> dao = new BaseEntityDao<>(
					Location.class, database);
			location = new Location();
			location.setCode(code);
			location.setRefId(refId);
			location.setName(code);
			return dao.insert(location);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Locations.class);
			log.error("failed to insert location: " + code, e);
			return null;
		}
	}
}
