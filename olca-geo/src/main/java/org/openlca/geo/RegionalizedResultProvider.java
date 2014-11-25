package org.openlca.geo;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.geo.kml.KmlFeature;

import java.util.Map;

public class RegionalizedResultProvider {

	private ContributionResultProvider<?> baseResult;
	private ContributionResultProvider<?> regionalizedResult;
	private Map<LongPair, KmlFeature> kmlFeatures;

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

	public void setKmlFeatures(Map<LongPair, KmlFeature> kmlFeatures) {
		this.kmlFeatures = kmlFeatures;
	}

	public Map<LongPair, KmlFeature> getKmlFeatures() {
		return kmlFeatures;
	}
}
