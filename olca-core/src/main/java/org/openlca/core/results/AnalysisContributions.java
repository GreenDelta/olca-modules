package org.openlca.core.results;

import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class AnalysisContributions {

	private final LinkContributions linkContributions;
	private final ContributionTreeCalculator treeCalculator;

	AnalysisContributions(AnalysisResult result,
			LinkContributions linkContributions) {
		this.linkContributions = linkContributions;
		this.treeCalculator = new ContributionTreeCalculator(result,
				linkContributions);
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to
	 * the product input (recipient) of the given link and the calculated
	 * product system. The returned share is a value between 0 and 1.
	 */
	public double getLinkShare(ProcessLink link) {
		return linkContributions.getShare(link);
	}

	public ContributionTree getTree(FlowDescriptor flow) {
		return treeCalculator.calculate(flow);
	}

	public ContributionTree getTree(ImpactCategoryDescriptor impact) {
		return treeCalculator.calculate(impact);
	}

}
