package org.openlca.geo.parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores parameter values for locations. If a location does not have location
 * specific values the default parameter values are returned for this location.
 */
public class ParameterSet {

	private Map<Long, Map<String, Double>> specificValues = new HashMap<>();
	private Map<String, Double> defaultValues = new HashMap<>();

	ParameterSet(Map<String, Double> defaultValues) {
		this.defaultValues = defaultValues;
	}

	void put(long locationId, Map<String, Double> map) {
		specificValues.put(locationId, map);
	}

	public Map<String, Double> get(long locationId) {
		if (!specificValues.containsKey(locationId))
			return defaultValues;
		return specificValues.get(locationId);
	}
}
