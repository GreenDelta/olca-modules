package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongIndex;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public final class InventoryResults {

	private InventoryResults() {
	}

	public static Set<FlowDescriptor> getFlows(InventoryResult result,
			Cache cache) {
		return Results.getFlowDescriptors(result.getFlowIndex(), cache);
	}

	public static Set<ImpactCategoryDescriptor> getImpacts(
			InventoryResult result, Cache cache) {
		LongIndex impactIndex = result.getImpactIndex();
		if (impactIndex == null)
			return Collections.emptySet();
		return Results.getImpactDescriptors(impactIndex, cache);
	}

	/**
	 * Returns the flow results of the inventory results. *No* entries are
	 * generated for 0-values.
	 */
	public static List<FlowResult> getFlowResults(InventoryResult result,
			Cache cache) {
		List<FlowResult> results = new ArrayList<>();
		FlowIndex flowIndex = result.getFlowIndex();
		for (FlowDescriptor d : getFlows(result, cache)) {
			double val = result.getFlowResult(d.getId());
			if (val == 0)
				continue;
			FlowResult r = new FlowResult();
			r.setFlow(d);
			r.setInput(flowIndex.isInput(d.getId()));
			r.setValue(val);
			results.add(r);
		}
		return results;
	}

	/**
	 * Returns the impact category results for the given result. In contrast to
	 * the flow results, entries are also generated for 0-values.
	 */
	public static List<ImpactCategoryResult> getImpactResults(
			InventoryResult result, Cache cache) {
		List<ImpactCategoryResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor d : getImpacts(result, cache)) {
			ImpactCategoryResult r = createImpactResult(result, d);
			results.add(r);
		}
		return results;
	}

	public static List<ImpactCategoryResult> getImpactResults(
			InventoryResult result, NormalizationWeightingSet nwset, Cache cache) {
		List<ImpactCategoryResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor d : getImpacts(result, cache)) {
			ImpactCategoryResult r = createImpactResult(result, d);
			results.add(r);
			NormalizationWeightingFactor factor = nwset.getFactor(d.getId());
			if (factor == null)
				continue;
			if (factor.getNormalizationFactor() != null)
				r.setNormalizationFactor(factor.getNormalizationFactor());
			if (factor.getWeightingFactor() != null)
				r.setWeightingFactor(factor.getWeightingFactor());
			r.setWeightingUnit(nwset.getUnit());
		}
		return results;
	}

	private static ImpactCategoryResult createImpactResult(
			InventoryResult result, ImpactCategoryDescriptor d) {
		double val = result.getImpactResult(d.getId());
		ImpactCategoryResult r = new ImpactCategoryResult();
		r.setImpactCategory(d);
		r.setValue(val);
		return r;
	}
}
