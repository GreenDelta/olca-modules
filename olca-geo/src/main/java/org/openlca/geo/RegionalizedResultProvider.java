package org.openlca.geo;

import java.util.List;

import org.openlca.core.results.ContributionResultProvider;
import org.openlca.geo.kml.KmlLoadResult;

public class RegionalizedResultProvider {

	private ContributionResultProvider<?> baseResult;
	private ContributionResultProvider<?> regionalizedResult;
	private List<KmlLoadResult> kmlData;

	public void setBaseResult(ContributionResultProvider<?> baseResult) {
		this.baseResult = baseResult;
	}

	public ContributionResultProvider<?> getBaseResult() {
		return baseResult;
	}

	public void setRegionalizedResult(
			ContributionResultProvider<?> regionalizedResult) {
		this.regionalizedResult = regionalizedResult;
	}

	public ContributionResultProvider<?> getRegionalizedResult() {
		return regionalizedResult;
	}

	public List<KmlLoadResult> getKmlData() {
		return kmlData;
	}

	public void setKmlData(List<KmlLoadResult> kmlData) {
		this.kmlData = kmlData;
	}

}
