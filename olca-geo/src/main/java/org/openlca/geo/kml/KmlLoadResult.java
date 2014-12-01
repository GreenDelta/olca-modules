package org.openlca.geo.kml;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.LongPair;

public class KmlLoadResult {

	private KmlFeature kmlFeature;
	private long locationId;
	private List<LongPair> processProducts = new ArrayList<>();

	public KmlLoadResult(KmlFeature kmlFeature, long locationId) {
		this.kmlFeature = kmlFeature;
		this.locationId = locationId;
	}

	public KmlFeature getKmlFeature() {
		return kmlFeature;
	}

	public long getLocationId() {
		return locationId;
	}

	public List<LongPair> getProcessProducts() {
		return processProducts;
	}

}
