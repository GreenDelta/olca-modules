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
import org.openlca.core.matrix.TechIndex;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the KML features for a given set of process products from a database.
 */
public class KmlLoader implements IKmlLoader {

	private Logger log = LoggerFactory.getLogger(getClass());

	protected final IDatabase database;

	protected final HashMap<Long, byte[]> locationKmz = new HashMap<>();
	protected final HashMap<Long, LocationKml> resultByLocationId = new HashMap<>();
	protected final Map<Long, Long> processLocations = new HashMap<>();

	public KmlLoader(IDatabase database) {
		this.database = database;
	}

	@Override
	public final List<LocationKml> load(TechIndex index) {
		if (index == null)
			return Collections.emptyList();
		try {
			loadProcessLocations(index);
			queryLocationTable();
			List<LocationKml> results = new ArrayList<>();
			for (int i = 0; i < index.size(); i++) {
				LongPair product = index.getProviderAt(i);
				LocationKml result = getFeatureResult(product.getFirst());
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

	private void loadProcessLocations(TechIndex idx) throws Exception {
		Set<Long> processIds = idx.getProcessIds();
		String query = "select id, f_location from tbl_processes";
		NativeSql.on(database).query(query, rs -> {
			long id = rs.getLong("id");
			if (!processIds.contains(id))
				return true;
			long locationId = rs.getLong("f_location");
			if (rs.wasNull())
				return true;
			processLocations.put(id, locationId);
			return true;
		});
	}

	private void queryLocationTable() throws Exception {
		String query = "select id, ref_id, kmz from tbl_locations";
		NativeSql.on(database).query(query, rs -> {
			long id = rs.getLong("id");
			if (!needToLoadLocation(id))
				return true;
			byte[] kmz = rs.getBytes("kmz");
			if (kmz != null)
				locationKmz.put(id, kmz);
			return true;
		});
	}

	protected boolean needToLoadLocation(Long id) {
		return processLocations.containsValue(id);
	}

	protected LocationKml getFeatureResult(Long processId) {
		Long locationId = processLocations.get(processId);
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

	protected final LocationKml createResult(long locationId, byte[] kmz) {
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
