package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.NwSetTable;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public final class InventoryImpactResults {

	private final InventoryResult result;

	public InventoryImpactResults(InventoryResult result) {
		this.result = result;
	}

	public Set<ImpactCategoryDescriptor> getImpacts(EntityCache cache) {
		LongIndex impactIndex = result.getImpactIndex();
		if (impactIndex == null)
			return Collections.emptySet();
		return Results.getImpactDescriptors(impactIndex, cache);
	}

	/**
	 * Returns the impact category results for the given result. In contrast to
	 * the flow results, entries are also generated for 0-values.
	 */
	public List<InventoryImpactResult> getAll(EntityCache cache) {
		List<InventoryImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor d : getImpacts(cache)) {
			InventoryImpactResult r = get(d);
			results.add(r);
		}
		return results;
	}

	public List<InventoryImpactResult> getAll(NwSetTable nwset,
			EntityCache cache) {
		List<InventoryImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor d : getImpacts(cache)) {
			InventoryImpactResult r = get(d, nwset);
			results.add(r);
		}
		return results;
	}

	public InventoryImpactResult get(ImpactCategoryDescriptor impact) {
		return get(impact, null);
	}

	public InventoryImpactResult get(ImpactCategoryDescriptor impact,
			NwSetTable nwset) {
		double val = result.getImpactResult(impact.getId());
		InventoryImpactResult r = new InventoryImpactResult();
		r.setImpactCategory(impact);
		r.setValue(val);
		if (nwset == null) {
			return r;
		}
		long impactId = impact.getId();
		r.setNormalizationFactor(nwset.getNormalisationFactor(impactId));
		r.setWeightingFactor(nwset.getWeightingFactor(impactId));
		return r;
	}

}
