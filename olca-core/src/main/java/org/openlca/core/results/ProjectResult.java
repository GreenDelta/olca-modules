package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import gnu.trove.set.hash.TLongHashSet;

/**
 * A project result is a wrapper for the inventory results of the respective
 * project variants.
 */
public class ProjectResult implements IResult {

	private final HashMap<ProjectVariant, ContributionResult> results = new HashMap<>();

	// cached descriptor lists which are initialized lazily
	private ArrayList<IndexFlow> _flows;
	private ArrayList<ImpactDescriptor> _impacts;
	private ArrayList<CategorizedDescriptor> _processes;
	private ArrayList<ProjectVariant> _variants;
	
	public void addResult(ProjectVariant variant, ContributionResult result) {
		results.put(variant, result);
	}

	public List<ProjectVariant> getVariants() {
		if (_variants != null)
			return _variants;
		if (results.isEmpty())
			return Collections.emptyList();
		_variants = new ArrayList<>();
		_variants.addAll(results.keySet());
		return _variants;
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

	public List<Contribution<ProjectVariant>> getContributions(IndexFlow flow) {
		return Contributions.calculate(
				getVariants(), variant -> getTotalFlowResult(variant, flow));
	}

	public double getTotalImpactResult(ProjectVariant variant,
			ImpactDescriptor impact) {
		ContributionResult result = results.get(variant);
		if (result == null)
			return 0;
		return result.getTotalImpactResult(impact);
	}

	public List<Contribution<ProjectVariant>> getContributions(
			ImpactDescriptor impact) {
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
	public final List<CategorizedDescriptor> getProcesses() {
		if (_processes != null)
			return _processes;
		_processes = new ArrayList<>();
		TLongHashSet handled = new TLongHashSet();
		for (ContributionResult result : results.values()) {
			for (CategorizedDescriptor p : result.getProcesses()) {
				if (handled.contains(p.id))
					continue;
				_processes.add(p);
				handled.add(p.id);
			}
		}
		return _processes;
	}

	@Override
	public List<IndexFlow> getFlows() {
		if (_flows != null)
			return _flows;
		HashSet<IndexFlow> flows = new HashSet<>();
		for (ContributionResult r : results.values()) {
			for (IndexFlow f : r.getFlows()) {
				flows.add(f);
			}
		}
		_flows = new ArrayList<>();
		_flows.addAll(flows);
		return _flows;
	}

	@Override
	public List<ImpactDescriptor> getImpacts() {
		if (_impacts != null)
			return _impacts;
		_impacts = new ArrayList<>();
		TLongHashSet handled = new TLongHashSet();
		for (ContributionResult r : results.values()) {
			if (!r.hasImpactResults())
				continue;
			for (ImpactDescriptor impact : r.getImpacts()) {
				if (handled.contains(impact.id))
					continue;
				_impacts.add(impact);
				handled.add(impact.id);
			}
		}
		return _impacts;
	}
}
