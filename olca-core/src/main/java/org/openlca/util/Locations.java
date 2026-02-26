package org.openlca.util;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

public class Locations {

	/**
	 * Performance improvement: Used in onlineLCA to get refId, name, code, longitude and
	 * latitude of locations instead of loading the whole model
	 */
	public static List<LocationInfo> allOf(IDatabase db) {
		var locations = new ArrayList<LocationInfo>();
		NativeSql.on(db).query("SELECT ref_id, name, code, longitude, latitude FROM tbl_locations", result -> {
			locations.add(new LocationInfo(
					result.getString(1),
					result.getString(2),
					result.getString(3),
					result.getDouble(4),
					result.getDouble(5)
					));
			return true;
		});
		return locations;
	}

	public record LocationInfo(String refId, String name, String code, double longitude, double latitude) {
	}
	
}
