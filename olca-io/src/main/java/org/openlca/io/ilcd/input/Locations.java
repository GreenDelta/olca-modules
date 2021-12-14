package org.openlca.io.ilcd.input;

import org.openlca.core.model.Location;
import org.openlca.util.KeyGen;

final class Locations {

	private Locations() {
	}

	/**
	 * Returns the location with the given code from the database or null if it
	 * not exists in the database.
	 */
	public static Location get(String code, ImportConfig config) {
		if (code == null || config.db() == null)
			return null;
		String refId = KeyGen.get(code);
		return config.db().get(Location.class, refId);
	}

	/**
	 * Returns the location with the given code from the database or creates a
	 * new one if it not yet exists.
	 */
	public static Location getOrCreate(String code, ImportConfig config) {
		if (code == null || config.db() == null)
			return null;
		Location location = get(code, config);
		if (location != null)
			return location;
		String refId = KeyGen.get(code);
		location = new Location();
		location.code = code;
		location.refId = refId;
		location.name = code;
		return config.db().insert(location);
	}
}
