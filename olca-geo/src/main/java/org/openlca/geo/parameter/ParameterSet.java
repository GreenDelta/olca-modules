package org.openlca.geo.parameter;

import java.util.HashMap;
import java.util.Map;

public class ParameterSet {

	private Map<Long, Map<String, Double>> maps = new HashMap<>();
	private Map<String, Double> defaults = new HashMap<>();

	ParameterSet(Map<String, Double> defaults) {
		this.defaults = defaults;
	}

	void put(long locationId, Map<String, Double> map) {
		maps.put(locationId, map);
	}

	public Map<String, Double> getFor(long locationId) {
		if (!maps.containsKey(locationId))
			return defaults;
		return maps.get(locationId);
	}

}
