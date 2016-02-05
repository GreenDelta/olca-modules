package org.openlca.geo.kml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the KML features for a given set of process products from a database.
 */
public class KmlLoader {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;

	private HashMap<Long, byte[]> locationKmz = new HashMap<>();
	private HashMap<Long, LocationKml> resultByLocationId = new HashMap<>();

	public KmlLoader(IDatabase database) {
		this.database = database;
	}

	public List<LocationKml> load(ProductIndex index) {
		if (index == null)
			return Collections.emptyList();
		try {
			Map<Long, Long> processLocs = getProcessLocations(index);
			queryLocationTable(processLocs);
			List<LocationKml> results = new ArrayList<>();
			for (int i = 0; i < index.size(); i++) {
				LongPair product = index.getProductAt(i);
				Long locationId = processLocs.get(product.getFirst());
				LocationKml result = getFeatureResult(locationId);
				if (result == null)
					continue;
				if (!results.contains(result))
					results.add(result);
				result.processProducts.add(product);
			}
			return results;
		} catch (Exception e) {
			log.error("failed to get KML data from database", e);
			return Collections.emptyList();
		}
	}

	private Map<Long, Long> getProcessLocations(ProductIndex idx) throws Exception {
		Map<Long, Long> m = new HashMap<>();
		Set<Long> processIds = idx.getProcessIds();
		String query = "select id, f_location from tbl_processes";
		NativeSql.on(database).query(query, rs -> {
			long id = rs.getLong("id");
			if (!processIds.contains(id))
				return true;
			long locationId = rs.getLong("f_location");
			if (rs.wasNull())
				return true;
			m.put(id, locationId);
			return true;
		});
		return m;
	}

	private void queryLocationTable(Map<Long, Long> processLocations) throws Exception {
		String query = "select id, ref_id, kmz from tbl_locations";
		NativeSql.on(database).query(query, rs -> {
			long id = rs.getLong("id");
			if (!processLocations.containsValue(id))
				return true;
			byte[] kmz = rs.getBytes("kmz");
			if (kmz != null)
				locationKmz.put(id, kmz);
			return true;
		});
	}

	private LocationKml getFeatureResult(Long locationId) {
		if (locationId == null)
			return null;
		LocationKml result = resultByLocationId.get(locationId);
		if (result != null)
			return result;
		byte[] locKmz = locationKmz.get(locationId);
		if (locKmz == null)
			return null;
		result = createResult(locationId, locKmz);
		resultByLocationId.put(locationId, result);
		return result;
	}

	private LocationKml createResult(long locationId, byte[] kmz) {
		if (kmz == null)
			return null;
		try {
			byte[] kmlBytes = BinUtils.unzip(kmz);
			String kml = new String(kmlBytes, "utf-8");
			return new LocationKml(KmlFeature.parse(kml), locationId);
		} catch (Exception e) {
			log.error("failed to parse KMZ", e);
			return null;
		}
	}

}
