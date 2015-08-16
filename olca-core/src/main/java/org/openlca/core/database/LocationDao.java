package org.openlca.core.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.util.Strings;

public class LocationDao extends RootEntityDao<Location, BaseDescriptor> {

	public LocationDao(IDatabase database) {
		super(Location.class, BaseDescriptor.class, database);
	}

	private final static String[] descriptorFields = { "id", "ref_id", "name",
			"description", "code", "latitude", "longitude" };

	public byte[] getKmz(long locationId) {
		try (Connection conn = getDatabase().createConnection()) {
			Statement s = conn.createStatement();
			ResultSet rs = s
					.executeQuery("SELECT kmz FROM tbl_locations WHERE id = "
							+ locationId);
			if (rs.next())
				return rs.getBytes("kmz");
		} catch (Exception e) {
			log.error("Error loading kmz data", e);
		}
		return null;
	}

	public List<Location> getAllWithoutKmz() {
		List<Object[]> results = selectAll(
				"SELECT " + Strings.join(descriptorFields, ',')
						+ " FROM tbl_locations", descriptorFields,
				Collections.emptyList());
		if (results == null)
			return Collections.emptyList();
		List<Location> result = new ArrayList<>();
		for (Object[] queryResult : results)
			result.add(createLightweight(queryResult));
		return result;
	}

	private Location createLightweight(Object[] queryResult) {
		if (queryResult == null)
			return null;
		Location descriptor = new Location();
		try {
			descriptor.setId((Long) queryResult[0]);
			descriptor.setRefId((String) queryResult[1]);
			descriptor.setName((String) queryResult[2]);
			descriptor.setDescription((String) queryResult[3]);
			descriptor.setCode((String) queryResult[4]);
			if (queryResult[5] != null)
				descriptor.setLatitude((double) queryResult[5]);
			if (queryResult[6] != null)
				descriptor.setLongitude((double) queryResult[6]);
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"failed to map query result to descriptor", e);
		}
		return descriptor;
	}

}
