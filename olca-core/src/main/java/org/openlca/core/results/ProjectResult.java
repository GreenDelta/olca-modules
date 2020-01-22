package org.openlca.core.results;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * A project result is a wrapper for the inventory results of the respective
 * project variants.
 */
public class ProjectResult implements IResult {

	private HashMap<ProjectVariant, ContributionResult> results = new HashMap<>();

	public void addResult(ProjectVariant variant, ContributionResult result) {
		results.put(variant, result);
	}

	public Set<ProjectVariant> getVariants() {
		return results.keySet();
	}

	public ContributionResult getResult(ProjectVariant variant) {
		return results.get(variant);
	}

	public double getTotalFlowResult(ProjectVariant variant, IndexFlow flow) {
		ContributionResult r = results.get(variant);
		if (r == null)
			return 0;
		return r.getTotalFlowResult(flow);
	}

	public List<FlowResult> getTotalFlowResults(ProjectVariant variant) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return Collections.emptyList();
		return result.getTotalFlowResults();
	}

	public ContributionSet<ProjectVariant> getContributions(IndexFlow flow) {
		return Contributions.calculate(
				getVariants(), variant -> getTotalFlowResult(variant, flow));
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
	public Set<IndexFlow> getFlows() {
		HashSet<IndexFlow> flows = new HashSet<>();
		for (ContributionResult sub : results.values()) {
			flows.addAll(sub.getFlows());
		}
		return flows;
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
