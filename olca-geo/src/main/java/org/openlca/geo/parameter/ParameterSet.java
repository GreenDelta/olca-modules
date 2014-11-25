package org.openlca.geo.parameter;

import java.util.HashMap;
import java.util.Map;

import org.openlca.geo.kml.KmlFeature;

public class ParameterSet {

	private Map<String, Map<String, Double>> maps = new HashMap<>();
	private Map<String, Double> defaults = new HashMap<>();

	ParameterSet(Map<String, Double> defaults) {
		this.defaults = defaults;
	}

	void put(KmlFeature feature, Map<String, Double> map) {
		maps.put(feature.getIdentifier(), map);
	}

	public Map<String, Double> getFor(KmlFeature feature) {
		if (!maps.containsKey(feature.getIdentifier()))
			return defaults;
		return maps.get(feature.getIdentifier());
	}

}
