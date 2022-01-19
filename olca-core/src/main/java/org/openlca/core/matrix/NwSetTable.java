package org.openlca.core.matrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.results.ImpactValue;

/**
 * Stores the factors of a normalization and weighting set.
 */
public class NwSetTable {

	private final HashMap<Long, Double> weightFactors = new HashMap<>();
	private final HashMap<Long, Double> normFactors = new HashMap<>();

	private NwSetTable() {
	}

	public static NwSetTable empty() {
		return new NwSetTable();
	}

	public static NwSetTable of(IDatabase db, NwSet nwSet) {
		return db == null || nwSet == null
				? empty()
				: of(db, nwSet.id);
	}

	public static NwSetTable of(IDatabase db, NwSetDescriptor d) {
		return db == null || d == null
				? empty()
				: of(db, d.id);
	}

	public static NwSetTable of(IDatabase db, long nwSetID) {
		var table = new NwSetTable();
		var sql = "select * from tbl_nw_factors where f_nw_set = " + nwSetID;
		NativeSql.on(db).query(sql, r -> {
			long categoryId = r.getLong("f_impact_category");
			double wf = r.getDouble("weighting_factor");
			if (!r.wasNull()) {
				table.weightFactors.put(categoryId, wf);
			}
			double nf = r.getDouble("normalisation_factor");
			if (!r.wasNull()) {
				table.normFactors.put(categoryId, nf);
			}
			return true;
		});
		return table;
	}

	public boolean isEmpty() {
		return weightFactors.isEmpty() && normFactors.isEmpty();
	}

	public boolean hasWeighting() {
		return !weightFactors.isEmpty();
	}

	public boolean hasNormalization() {
		return !normFactors.isEmpty();
	}

	public double getWeightingFactor(ImpactDescriptor impact) {
		return impact == null
				? 0
				: getWeightingFactor(impact.id);
	}

	public double getWeightingFactor(long impactID) {
		Double f = weightFactors.get(impactID);
		return f == null
				? 0
				: f;
	}

	public double getNormalizationFactor(ImpactDescriptor impact) {
		return impact == null
				? 0
				: getNormalizationFactor(impact.id);
	}

	public double getNormalizationFactor(long impactID) {
		Double f = normFactors.get(impactID);
		return f == null ? 0 : f;
	}

	/**
	 * Applies the normalization factors to the given impact assessment result.
	 * Returns a normalized result for each result item in the given list. The given
	 * list is not modified.
	 */
	public List<ImpactValue> normalize(List<ImpactValue> results) {
		return apply(results, 0);
	}

	/**
	 * Applies the weighting factors to the given impact assessment result. Returns
	 * a weighted result for each result item in the given list. The given list is
	 * not modified.
	 */
	public List<ImpactValue> weight(List<ImpactValue> results) {
		return apply(results, 1);
	}

	/**
	 * Applies the normalization and weighting factors to the given impact
	 * assessment result. Returns a normalized and weighted result for each result
	 * item in the given list. The given list is not modified.
	 */
	public List<ImpactValue> apply(List<ImpactValue> results) {
		return apply(results, 2);
	}

	/**
	 * Applies the factors in this table depending on the given type:
	 * <ul>
	 * <li>0: normalization
	 * <li>1: weighting
	 * <li>2: both
	 */
	private List<ImpactValue> apply(List<ImpactValue> results, int type) {
		return results == null
				? Collections.emptyList()
				: results.stream()
						.filter(r -> r.impact() != null)
						.map(r -> {
							double f = getFactor(type, r.impact().id);
							return ImpactValue.of(r.impact(), f * r.value());
						})
						.collect(Collectors.toList());
	}

	private double getFactor(int type, long impactId) {
		switch (type) {
		case 0:
			double nf = getNormalizationFactor(impactId);
			return nf == 0 ? 0 : 1 / nf;
		case 1:
			return getWeightingFactor(impactId);
		case 2:
			double nff = getNormalizationFactor(impactId);
			return nff == 0 ? 0 : getWeightingFactor(impactId) / nff;
		default:
			return 0;
		}
	}
}
