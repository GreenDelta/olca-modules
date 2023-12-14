package org.openlca.geo.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openlca.core.model.Location;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.geo.geojson.Geometry;
import org.openlca.util.Pair;

/**
 * Calculates the intersections of a geometry g (typically a location in
 * an openLCA database) with a given feature collection F (e.g. features
 * with characterization factors of an LCIA category). A specific projection can
 * be applied when calculating the intersections. By default, the Mollweide
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
		var features = new ArrayList<Feature>();
		var geometries = new ArrayList<org.locationtech.jts.geom.Geometry>();
		for (var feature : coll.features) {
			if (feature.geometry == null)
				continue;
			var g = feature.geometry;
			if (projection != null) {
				g = g.copy();
				projection.project(g);
			}
			var jts = JTS.fromGeoJSON(g);
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
		return jts(g).map(s -> Pair.of(s.feature, JTS.toGeoJSON(s.geometry)))
				.filter(p -> p.second != null)
				.collect(Collectors.toList());
	}

	/**
	 * Calculates the intersection shares of the given location.
	 */
	public List<IntersectionShare> shares(Location loc) {
		if (loc == null || loc.geodata == null)
			return List.of();
		var coll = GeoJSON.unpack(loc.geodata);
		if (coll == null || coll.features.isEmpty())
			return List.of();

		var shares = new ArrayList<IntersectionShare>();
		for (var f : coll.features) {
			if (f.geometry == null)
				continue;
			var next = shares(f.geometry);
			if (!next.isEmpty()) {
				shares.addAll(next);
			}
		}
		return shares;
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
	public List<IntersectionShare> shares(Geometry g) {

		// calculate the intersections
		var intersections = jts(g).toList();

		// select the maximum dimension
		int maxDim = intersections.stream().reduce(0,
				(dim, p) -> Math.max(dim, p.geometry.getDimension()),
				Math::max
		);

		// create the shares for that dimension
		var shares = new ArrayList<IntersectionShare>();
		for (var s : intersections) {
			double value = switch (maxDim) {
				case 0 -> s.geometry.getNumGeometries();
				case 1 -> s.geometry.getLength();
				case 2 -> s.geometry.getArea();
				default -> 0;
			};
			if (value > 0) {
				shares.add(IntersectionShare.of(s.feature, s.geometry, value));
			}
		}
		return IntersectionShare.makeRelative(shares);
	}

	/**
	 * Calculates the intersection geometries based on JTS geometries and
	 * returns the non-empty intersections.
	 */
	private Stream<Sect> jts(Geometry g) {
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
				.mapToObj(i -> Sect.of(features[i], geometries[i], jts))
				.filter(s -> !s.isEmpty());
	}

	private record Sect(
			Feature feature,
			org.locationtech.jts.geom.Geometry geometry) {

		static Sect of(
				Feature f,
				org.locationtech.jts.geom.Geometry g1,
				org.locationtech.jts.geom.Geometry g2) {
			try {
				var sect = g1.intersection(g2);
				return new Sect(f, sect);
			} catch (Exception e) {
				return new Sect(f, null);
			}
		}

		boolean isEmpty() {
			return feature == null
					|| geometry == null
					|| geometry.isEmpty();
		}
	}
}
