package org.openlca.geo;

import java.util.Map;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.results.ContributionResult;
import org.openlca.geo.kml.KmlFeature;

public class RegionalizedResult {

	private ContributionResult baseResult;
	private ContributionResult regionalizedResult;
	private Map<LongPair, KmlFeature> kmlFeatures;

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

	public void setKmlFeatures(Map<LongPair, KmlFeature> kmlFeatures) {
		this.kmlFeatures = kmlFeatures;
	}

	public Map<LongPair, KmlFeature> getKmlFeatures() {
		return kmlFeatures;
	}
}
