package org.openlca.geo.geojson;

import java.util.Map;

public class Feature {

	public Geometry geometry;

	/**
	 * Additional properties of this feature as a set of key-value pairs. We
	 * currently only support primitive types like numbers, booleans, and
	 * strings when de-/serializing a feature.
	 */
	public Map<String, Object> properties;

}
