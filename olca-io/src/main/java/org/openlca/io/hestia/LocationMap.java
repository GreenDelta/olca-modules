package org.openlca.io.hestia;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Location;
import org.openlca.io.maps.CountryInfo;

class LocationMap {

	/// Maps ISO-3-letter codes to the openLCA reference ID of a location / country
	private final Map<String, String> codeMap = new HashMap<>();
	private final IDatabase db;

	private LocationMap(IDatabase db) {
		this.db = db;
	}

	static LocationMap of(IDatabase db) {
		var map = new LocationMap(db);
		for (var info : CountryInfo.getAll()) {
			map.codeMap.put(info.alpha3(), info.refId());
		}
		return map;
	}

	Location get(Site site) {
		if (site == null)
			return null;
		var country = site.country();
		if (country == null)
			return null;
		var id = country.id();
		if (id == null)
			return null;

		var parts = id.split("-");
		if (parts.length < 2)
			return null;
		var code = parts[1].strip().toUpperCase();
		var refId = codeMap.get(code);
		return refId != null
			? db.get(Location.class, refId)
			: null;
	}

}
