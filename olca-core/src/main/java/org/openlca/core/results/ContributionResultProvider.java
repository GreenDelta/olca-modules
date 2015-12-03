package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class ContributionResultProvider<T extends ContributionResult> extends
		SimpleResultProvider<T> {

	public ContributionResultProvider(T result, EntityCache cache) {
		super(result, cache);
	}

	/**
	 * Get the single flow results for the process with the given ID.
	 */
	public List<FlowResult> getSingleFlowResults(ProcessDescriptor process) {
		FlowIndex index = result.flowIndex;
		List<FlowResult> results = new ArrayList<>();
		for (FlowDescriptor flow : getFlowDescriptors()) {
			double val = result.getSingleFlowResult(process.getId(),
					flow.getId());
			val = adoptFlowResult(val, flow.getId());
			FlowResult r = new FlowResult();
			r.flow = flow;
			r.input = index.isInput(flow.getId());
			r.value = val;
			results.add(r);
		}
		return results;
	}

	public FlowResult getSingleFlowResult(ProcessDescriptor process,
			FlowDescriptor flow) {
		double val = result.getSingleFlowResult(process.getId(), flow.getId());
		val = adoptFlowResult(val, flow.getId());
		FlowResult r = new FlowResult();
		r.flow = flow;
		FlowIndex index = result.flowIndex;
		r.input = index.isInput(flow.getId());
		r.value = val;
		return r;
	}

	/**
	 * Get the single contributions of the processes to the total result of the
	 * given flow.
	 */
	public ContributionSet<ProcessDescriptor> getProcessContributions(
			FlowDescriptor flow) {
		final long flowId = flow.getId();
		double total = adoptFlowResult(result.getTotalFlowResult(flowId),
				flowId);
		return Contributions.calculate(getProcessDescriptors(), total,
				process -> {
					double val = result.getSingleFlowResult(
							process.getId(), flowId);
					return adoptFlowResult(val, flowId);
				});
	}

	/**
	 * Get the single impact results for the process with the given ID.
	 */
	public List<ImpactResult> getSingleImpactResults(ProcessDescriptor process) {
		List<ImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor impact : getImpactDescriptors())
			results.add(getSingleImpactResult(process, impact));
		return results;
	}

	public ImpactResult getSingleImpactResult(ProcessDescriptor process,
			ImpactCategoryDescriptor impact) {
		double val = result.getSingleImpactResult(process.getId(),
				impact.getId());
		ImpactResult r = new ImpactResult();
		r.impactCategory = impact;
		r.value = val;
		return r;
	}

	/**
	 * Get the single contributions of the processes to the total result of the
	 * given LCIA category.
	 */
	public ContributionSet<ProcessDescriptor> getProcessContributions(
			ImpactCategoryDescriptor impact) {
		double total = result.getTotalImpactResult(impact.getId());
		return Contributions.calculate(getProcessDescriptors(), total,
				process -> result.getSingleImpactResult(process.getId(), impact.getId()));
	}

	public List<FlowResult> getSingleFlowImpacts(ImpactCategoryDescriptor impact) {
		List<FlowResult> results = new ArrayList<>();
		for (FlowDescriptor flow : getFlowDescriptors()) {
			FlowResult r = getSingleFlowImpact(flow, impact);
			results.add(r);
		}
		return results;
	}

	private FlowResult getSingleFlowImpact(FlowDescriptor flow,
			ImpactCategoryDescriptor impact) {
		FlowIndex index = result.flowIndex;
		double val = result.getSingleFlowImpact(flow.getId(), impact.getId());
		FlowResult r = new FlowResult();
		r.flow = flow;
		r.input = index.isInput(flow.getId());
		r.value = val;
		return r;
	}

	/**
	 * Get the single contributions of the flows to the total result of the
	 * given LCIA category.
	 */
	public ContributionSet<FlowDescriptor> getFlowContributions(
			final ImpactCategoryDescriptor impact) {
		double total = result.getTotalImpactResult(impact.getId());
		return Contributions.calculate(getFlowDescriptors(), total,
				flow -> result.getSingleFlowImpact(flow.getId(), impact.getId()));
	}

	public double getSingleCostResult(ProcessDescriptor process) {
		return result.getSingleCostResult(process.getId());
	}

	public ContributionSet<ProcessDescriptor> getProcessCostContributions() {
		return Contributions.calculate(
				getProcessDescriptors(),
				result.totalCostResult,
				process -> result.getSingleCostResult(process.getId()));
	}

}
