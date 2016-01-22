package org.openlca.geo;

import org.openlca.core.results.FullResult;

public class RegionalizedResult {

	public final FullResult baseResult;
	public final FullResult regionalizedResult;

	RegionalizedResult(FullResult baseResult, FullResult regioResult) {
		this.baseResult = baseResult;
		this.regionalizedResult = regioResult;
	}

}
