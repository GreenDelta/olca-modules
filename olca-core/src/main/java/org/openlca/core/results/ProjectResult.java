package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.LongPair;
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
		// in each variant the flow can be at a different location
		// in the respective flow index
		ContributionResult r = results.get(variant);
		if (r == null || r.flowIndex == null)
			return 0;
		int idx = r.flowIndex.of(flow.flow, flow.location);
		if (idx < 0)
			return 0;
		IndexFlow mapped = r.flowIndex.at(idx);
		return r.getTotalFlowResult(mapped);
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
	public List<IndexFlow> getFlows() {
		// a project result is a multi-flow index result.
		// we use the flow and location descriptors to
		// locate values in the respective sub-results
		HashSet<LongPair> handled = new HashSet<>();
		ArrayList<IndexFlow> flows = new ArrayList<>();
		for (ContributionResult sub : results.values()) {
			if (sub.flowIndex == null)
				continue;
			sub.flowIndex.each(f -> {
				if (f.flow == null)
					return;
				long flowID = f.flow.id;
				long locID = f.location != null
						? f.location.id
						: 0L;
				LongPair key = LongPair.of(flowID, locID);
				if (handled.contains(key))
					return;
				flows.add(f);
				handled.add(key);
			});
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
