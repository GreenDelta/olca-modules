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
 * with characterization factors of an LCIA category). A specific projection can
 * be applied when calculating the intersections. By default the Mollweide
 * projection is used.
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
	private final org.locationtech.jts.geom.Geometry[] geometries;

	private final Projection projection;

	private IntersectionCalculator(
			Feature[] features,
			org.locationtech.jts.geom.Geometry[] geometries,
			Projection projection) {
		this.features = features;
		this.geometries = geometries;
		this.projection = projection;
	}

	public static IntersectionCalculator on(FeatureCollection coll) {
		return on(coll, new Mollweide());
	}

	public static IntersectionCalculator on(
			FeatureCollection coll, Projection projection) {
		if (coll == null) {
			return new IntersectionCalculator(
					new Feature[0],
					new org.locationtech.jts.geom.Geometry[0],
					projection);
		}

		// we make sure that there is always a corresponding JTS geometry
		// for a feature.
		List<Feature> features = new ArrayList<>();
		List<org.locationtech.jts.geom.Geometry> geometries = new ArrayList<>();
		for (Feature feature : coll.features) {
			if (feature.geometry == null)
				continue;
			Geometry g = feature.geometry;
			if (projection != null) {
				g = g.copy();
				projection.project(g);
			}
			org.locationtech.jts.geom.Geometry jts = JTS.fromGeoJSON(g);
			if (jts == null)
				continue;
			features.add(feature);
			geometries.add(jts);
		}

		return new IntersectionCalculator(
				features.toArray(new Feature[0]),
				geometries.toArray(new org.locationtech.jts.geom.Geometry[0]),
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
	 * Calculates the intersection shares of the geometries in F with the given
	 * geometry g. Only the non-zero relative shares are returned: (0, 1]. The
	 * calculation of the share is based on the maximum dimension of the
	 * resulting geometries of the intersection, which is:
	 * <ol>
	 *     <li>area, for dimension = 2</li>
	 *     <li>length, for dimension = 1</li>
	 *     <li>number of geometries, for dimension = 0</li>
	 * </ol>
	 */
	public List<Pair<Feature, Double>> shares(Geometry g) {
		List<Pair<Feature, org.locationtech.jts.geom.Geometry>> s = jts(g)
				.collect(Collectors.toList());

		// get the maximum dimension
		int maxDim = s.stream().reduce(0,
				(dim, p) -> Math.max(dim, p.second.getDimension()),
				Math::max
		);

		// calculate the shares
		List<Pair<Feature, Double>> shares = s
				.parallelStream()
				.map(p -> {
					double a = 0;
					switch (maxDim) {
						case 0:
							a = p.second.getNumGeometries();
							break;
						case 1:
							a = p.second.getLength();
							break;
						case 2:
							a = p.second.getArea();
							break;
					}
					return Pair.of(p.first, a);
				})
				.filter(p -> p.second != null && p.second > 0)
				.collect(Collectors.toList());

		if (shares.isEmpty())
			return shares;

		// calculate the relative shares; above we made sure that
		// the total amount is > 0
		double total = 0;
		for (Pair<Feature, Double> p : shares) {
			total = Math.max(total, p.second);
		}
		for (Pair<Feature, Double> p : shares) {
			p.second = p.second / total;
		}
		return shares;
	}

	/**
	 * Calculates the intersection geometries based on JTS geometries and
	 * returns the non-empty intersections.
	 */
	private Stream<Pair<Feature, org.locationtech.jts.geom.Geometry>> jts(
			Geometry g) {
		if (g == null)
			return Stream.empty();
		org.locationtech.jts.geom.Geometry jts;
		if (projection == null) {
			jts = JTS.fromGeoJSON(g);
		} else {
			Geometry clone = g.copy();
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
