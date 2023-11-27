package org.openlca.geo.calc;

import org.openlca.geo.geojson.Feature;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the value of some property (e.g. polygon area, line length, number
 * of points) of a feature.
 */
public record FeatureShare(Feature feature, double value) {

	public static FeatureShare of(Feature feature, double value) {
		return new FeatureShare(feature, value);
	}

	public FeatureShare relativeTo(double total) {
		return total == 0
				? new FeatureShare(feature, 0)
				: new FeatureShare(feature, value / total);
	}

	public static List<FeatureShare> makeRelative(List<FeatureShare> shares) {
		if (shares == null || shares.isEmpty())
			return List.of();
		double max = shares.get(0).value;
		for (int i = 1; i < shares.size(); i++) {
			max = Math.max(max, shares.get(i).value);
		}
		var rels = new ArrayList<FeatureShare>(shares.size());
		for (var s : shares) {
			rels.add(s.relativeTo(max));
		}
		return rels;
	}
}
