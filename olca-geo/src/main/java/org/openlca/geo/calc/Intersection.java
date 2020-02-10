package org.openlca.geo.calc;

import org.openlca.geo.geojson.Geometry;

public class Intersection {

	private Intersection() {
	}

	public static Geometry of(Geometry a, Geometry b) {
		if (a == null || b == null)
			return null;
		return JTS.toGeoJSON(JTS.fromGeoJSON(a)
				.intersection(JTS.fromGeoJSON(b)));
	}

}
