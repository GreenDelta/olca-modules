package org.openlca.geo.kml;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
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
public class KmlLoader implements IKmlLoader {

	private Logger log = LoggerFactory.getLogger(getClass());

	protected final IDatabase database;

	protected HashMap<Long, Long> processLocations = new HashMap<>();
	protected HashMap<Long, byte[]> locationKmz = new HashMap<>();
	protected HashMap<Long, String> locationReferenceIds = new HashMap<>();
	protected HashMap<Long, KmlFeature> locationFeatures = new HashMap<>();

	public KmlLoader(IDatabase database) {
		this.database = database;
	}

	@Override
	public Map<LongPair, KmlFeature> load(ProductIndex index) {
		try {
			Set<Long> processIds = index.getProcessIds();
			queryProcessTable(processIds);
			queryLocationTable();
			Map<LongPair, KmlFeature> features = new HashMap<>();
			for (int i = 0; i < index.size(); i++) {
				LongPair processProduct = index.getProductAt(i);
				KmlFeature feature = getFeature(processProduct);
				if (feature != null)
					features.put(processProduct, feature);
			}
			return features;
		} catch (Exception e) {
			log.error("failed to get KML data from database", e);
			return Collections.emptyMap();
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
		String refId = resultSet.getString("ref_id");
		if (!processLocations.containsValue(id))
			return;
		byte[] kmz = resultSet.getBytes("kmz");
		if (kmz != null) {
			locationKmz.put(id, kmz);
			locationReferenceIds.put(id, refId);
		}
	}

	protected KmlFeature getFeature(LongPair processProduct) {
		if (processProduct == null)
			return null;
		long processId = processProduct.getFirst();
		Long locationId = processLocations.get(processId);
		if (locationId == null)
			return null;
		KmlFeature feature = locationFeatures.get(locationId);
		if (feature != null)
			return feature;
		byte[] locKmz = locationKmz.get(locationId);
		if (locKmz == null)
			return null;
		String referenceId = locationReferenceIds.get(locationId);
		feature = createFeature(referenceId, locKmz);
		locationFeatures.put(locationId, feature);
		return feature;
	}

	protected KmlFeature createFeature(String referenceId, byte[] kmz) {
		if (kmz == null)
			return null;
		try {
			byte[] kmlBytes = BinUtils.unzip(kmz);
			String kml = new String(kmlBytes, "utf-8");
			KmlFeature feature = KmlFeature.parse(kml);
			feature.setIdentifier(referenceId);
			return feature;
		} catch (Exception e) {
			log.error("failed to parse KMZ", e);
			return null;
		}
	}
}
