package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A project result is a wrapper for the inventory results of the respective
 * project variants.
 */
public class ProjectResultProvider implements IResultProvider {

	private HashMap<ProjectVariant, ContributionResultProvider<?>> results = new HashMap<>();
	public final EntityCache cache;

	public ProjectResultProvider(EntityCache cache) {
		this.cache = cache;
	}

	public void addResult(ProjectVariant variant, ContributionResult result) {
		ContributionResultProvider<?> provider = new ContributionResultProvider<>(
				result, cache);
		results.put(variant, provider);
	}

	public Set<ProjectVariant> getVariants() {
		return results.keySet();
	}

	public ContributionResultProvider<?> getResult(ProjectVariant variant) {
		return results.get(variant);
	}

	public FlowResult getTotalFlowResult(ProjectVariant variant,
			FlowDescriptor flow) {
		ContributionResultProvider<?> result = results.get(variant);
		if (result == null)
			return null;
		return result.getTotalFlowResult(flow);
	}

	public List<FlowResult> getTotalFlowResults(ProjectVariant variant) {
		ContributionResultProvider<?> result = results.get(variant);
		if (result == null)
			return Collections.emptyList();
		return result.getTotalFlowResults();
	}

	public ContributionSet<ProjectVariant> getContributions(FlowDescriptor flow) {
		return Contributions.calculate(getVariants(),
				variant -> getTotalFlowResult(variant, flow).value);
	}

	public ImpactResult getTotalImpactResult(ProjectVariant variant,
			ImpactCategoryDescriptor impact) {
		ContributionResultProvider<?> result = results.get(variant);
		if (result == null)
			return null;
		return result.getTotalImpactResult(impact);
	}

	public ContributionSet<ProjectVariant> getContributions(
			ImpactCategoryDescriptor impact) {
		return Contributions.calculate(getVariants(),
				variant -> getTotalImpactResult(variant, impact).value);
	}

	@Override
	public boolean hasImpactResults() {
		for (ContributionResultProvider<?> result : results.values())
			if (result.hasImpactResults())
				return true;
		return false;
	}

	@Override
	public boolean hasCostResults() {
		for (ContributionResultProvider<?> result : results.values()) {
			if (result.hasCostResults())
				return true;
		}
		return false;
	}

	@Override
	public Set<ProcessDescriptor> getProcessDescriptors() {
		Set<ProcessDescriptor> processes = new HashSet<>();
		for (ContributionResultProvider<?> result : results.values())
			processes.addAll(result.getProcessDescriptors());
		return processes;
	}

	@Override
	public Set<FlowDescriptor> getFlowDescriptors() {
		Set<FlowDescriptor> flows = new HashSet<>();
		for (ContributionResultProvider<?> result : results.values())
			flows.addAll(result.getFlowDescriptors());
		return flows;
	}

	@Override
	public Set<ImpactCategoryDescriptor> getImpactDescriptors() {
		Set<ImpactCategoryDescriptor> impacts = new HashSet<>();
		for (ContributionResultProvider<?> result : results.values())
			impacts.addAll(result.getImpactDescriptors());
		return impacts;
	}

	@Override
	public boolean isInput(FlowDescriptor flow) {
		if (flow == null)
			return false;
		for (ContributionResultProvider<?> r : results.values()) {
			if (r.getFlowDescriptors().contains(flow))
				return r.isInput(flow);
		}
		return false;
	}
}
