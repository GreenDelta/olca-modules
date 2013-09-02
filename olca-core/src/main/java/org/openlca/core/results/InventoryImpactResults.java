package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.matrices.LongIndex;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public final class InventoryImpactResults {

	private final InventoryResult result;

	public InventoryImpactResults(InventoryResult result) {
		this.result = result;
	}

	public Set<ImpactCategoryDescriptor> getImpacts(Cache cache) {
		LongIndex impactIndex = result.getImpactIndex();
		if (impactIndex == null)
			return Collections.emptySet();
		return Results.getImpactDescriptors(impactIndex, cache);
	}

	/**
	 * Returns the impact category results for the given result. In contrast to
	 * the flow results, entries are also generated for 0-values.
	 */
	public List<InventoryImpactResult> getAll(Cache cache) {
		List<InventoryImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor d : getImpacts(cache)) {
			InventoryImpactResult r = get(d);
			results.add(r);
		}
		return results;
	}

	public List<InventoryImpactResult> getAll(NormalizationWeightingSet nwset,
			Cache cache) {
		List<InventoryImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor d : getImpacts(cache)) {
			InventoryImpactResult r = get(d, nwset);
			results.add(r);
		}
		return results;
	}

	public InventoryImpactResult get(ImpactCategoryDescriptor d) {
		return get(d, null);
	}

	public InventoryImpactResult get(ImpactCategoryDescriptor d,
			NormalizationWeightingSet nwset) {
		double val = result.getImpactResult(d.getId());
		InventoryImpactResult r = new InventoryImpactResult();
		r.setImpactCategory(d);
		r.setValue(val);
		if (nwset == null)
			return r;
		NormalizationWeightingFactor factor = nwset.getFactor(d.getId());
		if (factor == null)
			return r;
		if (factor.getNormalizationFactor() != null)
			r.setNormalizationFactor(factor.getNormalizationFactor());
		if (factor.getWeightingFactor() != null)
			r.setWeightingFactor(factor.getWeightingFactor());
		r.setWeightingUnit(nwset.getUnit());
		return r;
	}

}
