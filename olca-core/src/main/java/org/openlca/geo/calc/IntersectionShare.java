package org.openlca.geo.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.openlca.geo.geojson.Feature;

/**
 * Contains the resulting value of an intersection calculation with a
 * geometry G.
 *
 * @param origin       the original feature that intersects with G
 * @param intersection the resulting geometry of the intersection
 * @param value        the share of this intersection which is, depending on the
 *                     resulting geometry, related to the area, length, or
 *                     number of points.
 */
public record IntersectionShare(
		Feature origin, Feature intersection, double value) {

	static IntersectionShare of(Feature origin, Geometry g, double value) {
		var intersection = new Feature();
		intersection.geometry = JTS.toGeoJSON(g);
		intersection.properties = origin.properties != null
				? new HashMap<>(origin.properties)
				: null;
		return new IntersectionShare(origin, intersection, value);
	}

	private IntersectionShare relativeTo(double total) {
		return total == 0
				? new IntersectionShare(origin, intersection, 0)
				: new IntersectionShare(origin, intersection, value / total);
	}

	public static List<IntersectionShare> makeRelative(List<IntersectionShare> shares) {
		if (shares == null || shares.isEmpty())
			return List.of();
		double max = shares.get(0).value;
		for (int i = 1; i < shares.size(); i++) {
			max = Math.max(max, shares.get(i).value);
		}
		var rels = new ArrayList<IntersectionShare>(shares.size());
		for (var s : shares) {
			rels.add(s.relativeTo(max));
		}
		return rels;
	}
}
