package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.Contributions.Function;

public class ContributionResultProvider<T extends ContributionResult> extends
		SimpleResultProvider<T> {

	public ContributionResultProvider(T result, EntityCache cache) {
		super(result, cache);
	}

	/** Get the single flow results for the process with the given ID. */
	public List<FlowResult> getSingleFlowResults(ProcessDescriptor process) {
		FlowIndex index = result.getFlowIndex();
		List<FlowResult> results = new ArrayList<>();
		for (FlowDescriptor flow : getFlowDescriptors()) {
			double val = result.getSingleFlowResult(process.getId(),
					flow.getId());
			if (val == 0)
				continue;
			val = adoptFlowResult(val, flow.getId());
			FlowResult r = new FlowResult();
			r.setFlow(flow);
			r.setInput(index.isInput(flow.getId()));
			r.setValue(val);
			results.add(r);
		}
		return results;
	}

	public FlowResult getSingleFlowResult(ProcessDescriptor process,
			FlowDescriptor flow) {
		double val = result.getSingleFlowResult(process.getId(), flow.getId());
		val = adoptFlowResult(val, flow.getId());
		FlowResult r = new FlowResult();
		r.setFlow(flow);
		FlowIndex index = result.getFlowIndex();
		r.setInput(index.isInput(flow.getId()));
		r.setValue(val);
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
				new Function<ProcessDescriptor>() {
					@Override
					public double value(ProcessDescriptor process) {
						double val = result.getSingleFlowResult(
								process.getId(), flowId);
						return adoptFlowResult(val, flowId);
					}
				});
	}

	/** Get the single impact results for the process with the given ID. */
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
		r.setImpactCategory(impact);
		r.setValue(val);
		return r;
	}

	/**
	 * Get the single contributions of the processes to the total result of the
	 * given LCIA category.
	 */
	public ContributionSet<ProcessDescriptor> getProcessContributions(
			final ImpactCategoryDescriptor impact) {
		double total = result.getTotalImpactResult(impact.getId());
		return Contributions.calculate(getProcessDescriptors(), total,
				new Function<ProcessDescriptor>() {
					@Override
					public double value(ProcessDescriptor process) {
						return result.getSingleImpactResult(process.getId(),
								impact.getId());
					}
				});
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
		FlowIndex index = result.getFlowIndex();
		double val = result.getSingleFlowImpact(flow.getId(), impact.getId());
		FlowResult r = new FlowResult();
		r.setFlow(flow);
		r.setInput(index.isInput(flow.getId()));
		r.setValue(val);
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
				new Function<FlowDescriptor>() {
					@Override
					public double value(FlowDescriptor flow) {
						return result.getSingleFlowImpact(flow.getId(),
								impact.getId());
					}
				});
	}

}
