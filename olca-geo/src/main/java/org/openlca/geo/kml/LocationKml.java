package org.openlca.geo.kml;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.matrix.LongPair;

/**
 * Stores a KML feature and a location ID. It also contains a list of process
 * products that can be used in calculations to identify which processes have
 * this location.
 */
public class LocationKml {

	public final KmlFeature kmlFeature;
	public final long locationId;
	public final List<LongPair> processProducts = new ArrayList<>();

	public LocationKml(KmlFeature kmlFeature, long locationId) {
		this.kmlFeature = kmlFeature;
		this.locationId = locationId;
	}
}
