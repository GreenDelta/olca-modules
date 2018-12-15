package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A project result is a wrapper for the inventory results of the respective
 * project variants.
 */
public class ProjectResultProvider implements IResult {

	private HashMap<ProjectVariant, ContributionResult> results = new HashMap<>();
	public final EntityCache cache;

	public ProjectResultProvider(EntityCache cache) {
		this.cache = cache;
	}

	public void addResult(ProjectVariant variant, ContributionResult result) {
		results.put(variant, result);
	}

	public Set<ProjectVariant> getVariants() {
		return results.keySet();
	}

	public ContributionResult getResult(ProjectVariant variant) {
		return results.get(variant);
	}

	public double getTotalFlowResult(ProjectVariant variant,
			FlowDescriptor flow) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return 0;
		return result.getTotalFlowResult(flow);
	}

	public List<FlowResult> getTotalFlowResults(ProjectVariant variant) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return Collections.emptyList();
		return result.getTotalFlowResults();
	}

	public ContributionSet<ProjectVariant> getContributions(
			FlowDescriptor flow) {
		return Contributions.calculate(getVariants(),
				variant -> getTotalFlowResult(variant, flow));
	}

	public double getTotalImpactResult(ProjectVariant variant,
			ImpactCategoryDescriptor impact) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return 0;
		return result.getTotalImpactResult(impact);
	}

	public ContributionSet<ProjectVariant> getContributions(
			ImpactCategoryDescriptor impact) {
		return Contributions.calculate(getVariants(),
				variant -> getTotalImpactResult(variant, impact));
	}

	@Override
	public boolean hasImpactResults() {
		for (ContributionResult result : results.values()) {
			if (result.hasImpactResults())
				return true;
		}
		return false;
	}

	@Override
	public boolean hasCostResults() {
		for (ContributionResult result : results.values()) {
			if (result.hasCostResults())
				return true;
		}
		return false;
	}

	@Override
	public boolean hasFlowResults() {
		for (ContributionResult result : results.values()) {
			if (result.hasFlowResults())
				return true;
		}
		return false;
	}

	@Override
	public Set<CategorizedDescriptor> getProcesses() {
		Set<CategorizedDescriptor> processes = new HashSet<>();
		for (ContributionResult result : results.values()) {
			processes.addAll(result.getProcesses());
		}
		return processes;
	}

	@Override
	public Set<FlowDescriptor> getFlows() {
		Set<FlowDescriptor> flows = new HashSet<>();
		for (ContributionResult result : results.values()) {
			flows.addAll(result.getFlows());
		}
		return flows;
	}

	@Override
	public boolean isInput(FlowDescriptor flow) {
		if (flow == null)
			return false;

		for (ContributionResult r : results.values()) {
			if (r.flowIndex.contains(flow))
				return r.flowIndex.isInput(flow);
		}
		return false;
	}

	@Override
	public Set<ImpactCategoryDescriptor> getImpacts() {
		Set<ImpactCategoryDescriptor> impacts = new HashSet<>();
		for (ContributionResult r : results.values()) {
			if (r.hasImpactResults()) {
				impacts.addAll(r.getImpacts());
			}
		}
		return impacts;
	}

}
