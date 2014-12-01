package org.openlca.geo;

import org.openlca.core.results.ContributionResult;

public class RegionalizedResult {

	private ContributionResult baseResult;
	private ContributionResult regionalizedResult;

	public void setBaseResult(ContributionResult baseResult) {
		this.baseResult = baseResult;
	}

	public ContributionResult getBaseResult() {
		return baseResult;
	}

	public void setRegionalizedResult(ContributionResult regionalizedResult) {
		this.regionalizedResult = regionalizedResult;
	}

	public ContributionResult getRegionalizedResult() {
		return regionalizedResult;
	}

}
