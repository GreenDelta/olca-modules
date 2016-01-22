package org.openlca.geo.kml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
public class KmlLoader implements IKmlLoader {

	private Logger log = LoggerFactory.getLogger(getClass());

	protected final IDatabase database;

	protected HashMap<Long, Long> processLocations = new HashMap<>();
	protected HashMap<Long, byte[]> locationKmz = new HashMap<>();
	protected HashMap<Long, KmlLoadResult> resultByLocationId = new HashMap<>();

	public KmlLoader(IDatabase database) {
		this.database = database;
	}

	@Override
	public List<KmlLoadResult> load(ProductIndex index) {
		try {
			Set<Long> processIds = index.getProcessIds();
			queryProcessTable(processIds);
			queryLocationTable();
			List<KmlLoadResult> results = new ArrayList<>();
			for (int i = 0; i < index.size(); i++) {
				LongPair product = index.getProductAt(i);
				KmlLoadResult result = getFeatureResult(product);
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

	protected void queryProcessTable(final Set<Long> processIds)
			throws Exception {
		String query = "select id, f_location from tbl_processes";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet resultSet) throws SQLException {
				registerProcessRow(resultSet, processIds);
				return true;
			}
		});
	}

	protected void registerProcessRow(ResultSet resultSet, Set<Long> processIds)
			throws SQLException {
		long id = resultSet.getLong("id");
		if (!processIds.contains(id))
			return;
		long locationId = resultSet.getLong("f_location");
		if (resultSet.wasNull())
			return;
		processLocations.put(id, locationId);
	}

	protected void queryLocationTable() throws Exception {
		String query = "select id, ref_id, kmz from tbl_locations";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet resultSet) throws SQLException {
				registerLocationRow(resultSet);
				return true;
			}
		});
	}

	protected void registerLocationRow(ResultSet resultSet) throws SQLException {
		long id = resultSet.getLong("id");
		if (!processLocations.containsValue(id))
			return;
		byte[] kmz = resultSet.getBytes("kmz");
		if (kmz != null)
			locationKmz.put(id, kmz);
	}

	protected KmlLoadResult getFeatureResult(LongPair processProduct) {
		if (processProduct == null)
			return null;
		long processId = processProduct.getFirst();
		Long locationId = processLocations.get(processId);
		if (locationId == null)
			return null;
		KmlLoadResult result = resultByLocationId.get(locationId);
		if (result != null)
			return result;
		byte[] locKmz = locationKmz.get(locationId);
		if (locKmz == null)
			return null;
		result = createResult(locationId, locKmz);
		resultByLocationId.put(locationId, result);
		return result;
	}

	protected KmlLoadResult createResult(long locationId, byte[] kmz) {
		if (kmz == null)
			return null;
		try {
			byte[] kmlBytes = BinUtils.unzip(kmz);
			String kml = new String(kmlBytes, "utf-8");
			return new KmlLoadResult(KmlFeature.parse(kml), locationId);
		} catch (Exception e) {
			log.error("failed to parse KMZ", e);
			return null;
		}
	}

}
