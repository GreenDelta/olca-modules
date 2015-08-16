package org.openlca.geo;

import org.openlca.core.results.FullResult;

public class RegionalizedResult {

	private FullResult baseResult;
	private FullResult regionalizedResult;

	public void setBaseResult(FullResult baseResult) {
		this.baseResult = baseResult;
	}

	public FullResult getBaseResult() {
		return baseResult;
	}

	public void setRegionalizedResult(FullResult regionalizedResult) {
		this.regionalizedResult = regionalizedResult;
	}

	public FullResult getRegionalizedResult() {
		return regionalizedResult;
	}

}
