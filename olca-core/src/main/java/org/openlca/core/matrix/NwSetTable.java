package org.openlca.core.matrix;

import gnu.trove.map.hash.TLongDoubleHashMap;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Stores the factors of a normalisation and weighting set. As creating such a
 * table is quite simple there is no separate builder class for reading
 * this kind of data from a database but a factory method in this class.
 */
public class NwSetTable {

	private final TLongDoubleHashMap weightingFactors = new TLongDoubleHashMap();
	private final TLongDoubleHashMap normalisationFactors = new TLongDoubleHashMap();

	/**
	 * Builds the table by reading the factors for the NW-set with the given ID
	 * from the given database.
	 */
	public static NwSetTable build(IDatabase database, long nwSetId) {
		final NwSetTable table = new NwSetTable();
		String query = "select * from tbl_nw_factors where f_nw_set = "
				+ nwSetId;
		try {
			NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
				@Override
				public boolean nextResult(ResultSet result) throws SQLException {
					fetchResult(result, table);
					return true;
				}
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(NwSetTable.class);
			log.error("failed to get nw-factors", e);
		}
		return table;
	}

	private static void fetchResult(ResultSet result, NwSetTable table)
			throws SQLException {
		long categoryId = result.getLong("f_impact_category");
		double weightingFactor = result.getDouble("weighting_factor");
		if (!result.wasNull())
			table.weightingFactors.put(categoryId, weightingFactor);
		double normalisationFactor = result.getDouble("normalisation_factor");
		if (!result.wasNull())
			table.normalisationFactors.put(categoryId, normalisationFactor);
	}

	/**
	 * Get the weighting factor for the given LCIA category.
	 */
	public double getWeightingFactor(long impactCategoryId) {
		return weightingFactors.get(impactCategoryId);
	}

	/**
	 * Get the normalisation factor for the given LCIA category.
	 */
	public double getNormalisationFactor(long impactCategoryId) {
		return normalisationFactors.get(impactCategoryId);
	}

}
