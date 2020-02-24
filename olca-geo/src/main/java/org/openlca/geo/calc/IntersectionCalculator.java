package org.openlca.geo.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
		return jts(g).map(p -> Pair.of(p.first, JTS.toGeoJSON(p.second)))
				.filter(p -> p.second != null)
				.collect(Collectors.toList());
	}

	/**
	 * Calculates the intersection geometries based on JTS geometries and
	 * returns the non-empty intersections.
	 */
	private Stream<Pair<Feature, com.vividsolutions.jts.geom.Geometry>> jts(
			Geometry g) {
		if (g == null)
			return Stream.empty();
		com.vividsolutions.jts.geom.Geometry jts;
		if (projection == null) {
			jts = JTS.fromGeoJSON(g);
		} else {
			Geometry clone = g.clone();
			projection.project(clone);
			jts = JTS.fromGeoJSON(clone);
		}
		if (jts == null)
			return Stream.empty();
		return IntStream.range(0, features.length)
				.parallel()
				.mapToObj(i -> Pair.of(
						features[i], geometries[i].intersection(jts)))
				.filter(p -> p.second != null && !p.second.isEmpty());
	}

}
