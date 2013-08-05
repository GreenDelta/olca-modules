package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.AnalysisResult;

/**
 * Calculates the contributions of the elementary flows to the impact category
 * results of an analysis result.
 */
public class FlowImpactContribution {

	private AnalysisResult result;
	private Process refProcess;

	public FlowImpactContribution(AnalysisResult result) {
		this.result = result;
		refProcess = result.getSetup().getReferenceProcess();
	}

	/** Calculate the flow contributions for the given impact. */
	public ContributionSet<Flow> calculate(ImpactCategoryDescriptor impact) {
		List<Contribution<Flow>> contributions = new ArrayList<>();
		for (Flow flow : result.getFlowIndex().getFlows()) {
			double lciVal = result.getResult(refProcess, flow);
			double factor = result.getImpactFactor(impact, flow);
			Contribution<Flow> contribution = new Contribution<>();
			contribution.setAmount(factor * lciVal);
			contribution.setItem(flow);
			contributions.add(contribution);
		}
		ContributionShare.calculate(contributions);
		return new ContributionSet<>(contributions);
	}
}
