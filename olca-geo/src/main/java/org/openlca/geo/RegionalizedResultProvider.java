package org.openlca.geo;

import java.util.List;

import org.openlca.core.results.FullResultProvider;
import org.openlca.geo.kml.KmlLoadResult;

public class RegionalizedResultProvider {

	private FullResultProvider baseResult;
	private FullResultProvider regionalizedResult;
	private List<KmlLoadResult> kmlData;

	public void setBaseResult(FullResultProvider baseResult) {
		this.baseResult = baseResult;
	}

	public FullResultProvider getBaseResult() {
		return baseResult;
	}

	public void setRegionalizedResult(FullResultProvider regionalizedResult) {
		this.regionalizedResult = regionalizedResult;
	}

	public FullResultProvider getRegionalizedResult() {
		return regionalizedResult;
	}

	public List<KmlLoadResult> getKmlData() {
		return kmlData;
	}

	public void setKmlData(List<KmlLoadResult> kmlData) {
		this.kmlData = kmlData;
	}

}
