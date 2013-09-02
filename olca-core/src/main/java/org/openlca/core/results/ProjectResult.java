package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.Contributions.Function;

/**
 * A project result is a wrapper for the inventory results of the respective
 * project variants.
 */
public class ProjectResult {

	private HashMap<ProjectVariant, InventoryResult> results = new HashMap<>();

	public void addResult(ProjectVariant variant, InventoryResult result) {
		results.put(variant, result);
	}

	public Set<ProjectVariant> getVariants() {
		return results.keySet();
	}

	public InventoryResult getResult(ProjectVariant variant) {
		return results.get(variant);
	}

	public Set<FlowDescriptor> getFlows(Cache cache) {
		Set<FlowDescriptor> flows = new HashSet<>();
		for (InventoryResult result : results.values())
			flows.addAll(result.getFlowResults().getFlows(cache));
		return flows;
	}

	public InventoryFlowResult getFlowResult(ProjectVariant variant,
			FlowDescriptor flow) {
		InventoryResult result = results.get(variant);
		if (result == null)
			return null;
		return result.getFlowResults().get(flow);
	}

	public List<InventoryFlowResult> getFlowResults(ProjectVariant variant,
			Cache cache) {
		InventoryResult result = results.get(variant);
		if (result == null)
			return Collections.emptyList();
		return result.getFlowResults().getAll(cache);
	}

	public ContributionSet<ProjectVariant> getContributions(
			final FlowDescriptor flow) {
		return Contributions.calculate(getVariants(),
				new Function<ProjectVariant>() {
					@Override
					public double value(ProjectVariant variant) {
						return getFlowResult(variant, flow).getValue();
					}
				});
	}

	public Set<ImpactCategoryDescriptor> getImpacts(Cache cache) {
		Set<ImpactCategoryDescriptor> impacts = new HashSet<>();
		for (InventoryResult result : results.values())
			impacts.addAll(result.getImpactResults().getImpacts(cache));
		return impacts;
	}

	public InventoryImpactResult getImpactResult(ProjectVariant variant,
			ImpactCategoryDescriptor impact) {
		InventoryResult result = results.get(variant);
		if (result == null)
			return null;
		return result.getImpactResults().get(impact);
	}

	public InventoryImpactResult getImpactResult(ProjectVariant variant,
			ImpactCategoryDescriptor impact, NormalizationWeightingSet nwSet) {
		InventoryResult result = results.get(variant);
		if (result == null)
			return null;
		return result.getImpactResults().get(impact, nwSet);
	}

	public ContributionSet<ProjectVariant> getContributions(
			final ImpactCategoryDescriptor impact) {
		return Contributions.calculate(getVariants(),
				new Function<ProjectVariant>() {
					@Override
					public double value(ProjectVariant variant) {
						return getImpactResult(variant, impact).getValue();
					}
				});
	}
}
