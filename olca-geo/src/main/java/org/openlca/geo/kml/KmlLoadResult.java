package org.openlca.geo.kml;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.LongPair;

public class KmlLoadResult {

	public final KmlFeature kmlFeature;
	public final long locationId;
	public final List<LongPair> processProducts = new ArrayList<>();

	public KmlLoadResult(KmlFeature kmlFeature, long locationId) {
		this.kmlFeature = kmlFeature;
		this.locationId = locationId;
	}
}
