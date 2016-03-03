package org.openlca.core.matrix;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.results.ImpactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the factors of a normalization and weighting set. As creating such a
 * table is quite simple there is no separate builder class for reading this
 * kind of data from a database but a factory method in this class.
 */
public class NwSetTable {

	private final HashMap<Long, Double> weightFactors = new HashMap<>();
	private final HashMap<Long, Double> normFactors = new HashMap<>();

	private boolean hasWeightFactors = false;
	private boolean hasNormFactors = false;

	/**
	 * Builds the table by reading the factors for the NW-set with the given ID
	 * from the given database.
	 */
	public static NwSetTable build(IDatabase database, long nwSetId) {
		final NwSetTable table = new NwSetTable();
		String query = "select * from tbl_nw_factors where f_nw_set = "
				+ nwSetId;
		try {
			NativeSql.on(database).query(query,
					new NativeSql.QueryResultHandler() {
						@Override
						public boolean nextResult(ResultSet result)
								throws SQLException {
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
		if (!result.wasNull()) {
			table.weightFactors.put(categoryId, weightingFactor);
			table.hasWeightFactors = true;
		}
		double normalisationFactor = result.getDouble("normalisation_factor");
		if (!result.wasNull()) {
			table.normFactors.put(categoryId, normalisationFactor);
			table.hasNormFactors = true;
		}
	}

	public boolean hasWeightingFactors() {
		return hasWeightFactors;
	}

	public boolean hasNormalisationFactors() {
		return hasNormFactors;
	}

	/**
	 * Get the weighting factor for the given LCIA category.
	 */
	public double getWeightingFactor(long impactCategoryId) {
		Double f = weightFactors.get(impactCategoryId);
		return f == null ? 0 : f;
	}

	/**
	 * Get the normalization factor for the given LCIA category.
	 */
	public double getNormalisationFactor(long impactCategoryId) {
		Double f = normFactors.get(impactCategoryId);
		return f == null ? 0 : f;
	}

	/**
	 * Applies the normalization factors to the given impact assessment result.
	 * Returns a normalized result for each result item in the given list. The
	 * given list is not modified.
	 */
	public List<ImpactResult> applyNormalisation(List<ImpactResult> results) {
		return apply(results, 0);
	}

	/**
	 * Applies the weighting factors to the given impact assessment result.
	 * Returns a weighted result for each result item in the given list. The
	 * given list is not modified.
	 */
	public List<ImpactResult> applyWeighting(List<ImpactResult> results) {
		return apply(results, 1);
	}

	/**
	 * Applies the normalization and weighting factors to the given impact
	 * assessment result. Returns a normalized and weighted result for each
	 * result item in the given list. The given list is not modified.
	 */
	public List<ImpactResult> applyBoth(List<ImpactResult> results) {
		return apply(results, 2);
	}

	/**
	 * Applies the factors in this table depending on the given type:
	 * <ul>
	 * <li>0: normalization
	 * <li>1: weighting
	 * <li>2: both
	 */
	private List<ImpactResult> apply(List<ImpactResult> results, int type) {
		if (results == null)
			return Collections.emptyList();
		List<ImpactResult> applied = new ArrayList<>();
		for (ImpactResult result : results) {
			if (result.impactCategory == null)
				continue;
			ImpactResult r = new ImpactResult();
			r.impactCategory = result.impactCategory;
			applied.add(r);
			long impactId = result.impactCategory.getId();
			double f = getFactor(type, impactId);
			r.value = f * result.value;
		}
		return applied;
	}

	private double getFactor(int type, long impactId) {
		switch (type) {
		case 0:
			double nf = getNormalisationFactor(impactId);
			return nf == 0 ? 0 : 1 / nf;
		case 1:
			return getWeightingFactor(impactId);
		case 2:
			double nff = getNormalisationFactor(impactId);
			return nff == 0 ? 0 : getWeightingFactor(impactId) / nff;
		default:
			return 0;
		}
	}

}
