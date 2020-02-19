package org.openlca.geo.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.Geometry;
import org.openlca.util.Pair;

/**
 * Calculates the intersections of a geometry $g$ (typically a location in
 * an openLCA database) with a given feature collection $F$ (e.g. features
 * with characterization factors of an LCIA category). An optional projection
 * can be applied when calculating the intersections.
 */
public class IntersectionCalculator {

	/**
	 * The raw, unchanged GeoJSON features with which this calculator was
	 * initialized.
	 */
	private final Feature[] features;

	/**
	 * The JTS geometries that we use for intersection calculations.
	 */
	private final com.vividsolutions.jts.geom.Geometry[] geometries;

	private final Projection projection;

	private IntersectionCalculator(
			Feature[] features,
			com.vividsolutions.jts.geom.Geometry[] geometries,
			Projection projection) {
		this.features = features;
		this.geometries = geometries;
		this.projection = projection;
	}

	public static IntersectionCalculator on(FeatureCollection coll) {
		return on(coll, null);
	}

	public static IntersectionCalculator on(
			FeatureCollection coll, Projection projection) {
		if (coll == null) {
			return new IntersectionCalculator(
					new Feature[0],
					new com.vividsolutions.jts.geom.Geometry[0],
					projection);
		}

		// we make sure that there is always a corresponding JTS geometry
		// for a feature.
		List<Feature> features = new ArrayList<>();
		List<com.vividsolutions.jts.geom.Geometry> geometries = new ArrayList<>();
		for (Feature feature : coll.features) {
			if (feature.geometry == null)
				continue;
			Geometry g = feature.geometry;
			if (projection != null) {
				g = g.clone();
				projection.project(g);
			}
			com.vividsolutions.jts.geom.Geometry jts = JTS.fromGeoJSON(g);
			if (jts == null)
				continue;
			features.add(feature);
			geometries.add(jts);
		}

		return new IntersectionCalculator(
				features.toArray(new Feature[0]),
				geometries.toArray(new com.vividsolutions.jts.geom.Geometry[0]),
				projection);
	}

	/**
	 * Returns a list of features and their corresponding intersection geometry
	 * with the given geometry.
	 */
	public List<Pair<Feature, Geometry>> calculate(Geometry g) {
		if (g == null)
			return Collections.emptyList();
		com.vividsolutions.jts.geom.Geometry jts = JTS.fromGeoJSON(g);
		if (jts == null)
			return Collections.emptyList();
		List<Pair<Feature, Geometry>> intersections = new ArrayList<>();
		for (int i = 0; i < features.length; i++) {
			com.vividsolutions.jts.geom.Geometry is = geometries[i].intersection(jts);
			if (is == null || is.isEmpty())
				continue;
			Geometry ig = JTS.toGeoJSON(is);
			if (ig == null)
				continue;
			if (projection != null) {
				projection.unproject(ig);
			}
			intersections.add(Pair.of(features[i], ig));
		}
		return intersections;
	}

}
