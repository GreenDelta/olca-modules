package org.openlca.geo.calc;

import java.util.function.Consumer;

import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.Geometry;
import org.openlca.geo.geojson.GeometryCollection;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.MultiLineString;
import org.openlca.geo.geojson.MultiPoint;
import org.openlca.geo.geojson.MultiPolygon;
import org.openlca.geo.geojson.Point;
import org.openlca.geo.geojson.Polygon;

public abstract class Projection {

	/**
	 * Applies the projection on the given point.
	 */
	protected abstract void apply(Point point);

	/**
	 * Applies the inverse projection on the given point.
	 */
	protected abstract void inverse(Point point);

	/**
	 * Applies the projection on the given geometry.
	 */
	public void project(Geometry g) {
		iterPoints(g, this::apply);
	}

	/**
	 * Applies the projection on the geometry of the given feature.
	 */
	public void project(Feature f) {
		if (f == null || f.geometry == null)
			return;
		project(f.geometry);
	}

	/**
	 * Applies the projection on the geometries of the given features.
	 */
	public void project(FeatureCollection features) {
		if (features == null)
			return;
		for (Feature f : features.features) {
			project(f);
		}
	}

	/**
	 * Applies the inverse projection on the given geometry.
	 */
	public void unproject(Geometry g) {
		iterPoints(g, this::inverse);
	}

	/**
	 * Applies the inverse projection on the geometry of the given feature.
	 */
	public void unproject(Feature f) {
		if (f == null || f.geometry == null)
			return;
		unproject(f.geometry);
	}

	/**
	 * Applies the inverse projection on the geometries of the given features.
	 */
	public void unproject(FeatureCollection features) {
		if (features == null)
			return;
		for (Feature f : features.features) {
			unproject(f);
		}
	}

	private void iterPoints(Geometry g, Consumer<Point> fn) {
		if (g == null)
			return;
		if (g instanceof Point) {
			fn.accept((Point) g);
			return;
		}
		if (g instanceof MultiPoint) {
			MultiPoint mp = (MultiPoint) g;
			for (Point point : mp.points) {
				fn.accept(point);
			}
			return;
		}
		if (g instanceof LineString) {
			LineString line = (LineString) g;
			for (Point point : line.points) {
				fn.accept(point);
			}
			return;
		}
		if (g instanceof MultiLineString) {
			MultiLineString mls = (MultiLineString) g;
			for (LineString line : mls.lineStrings) {
				iterPoints(line, fn);
			}
			return;
		}
		if (g instanceof Polygon) {
			Polygon polygon = (Polygon) g;
			for (LineString ring : polygon.rings) {
				iterPoints(ring, fn);
			}
			return;
		}
		if (g instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) g;
			for (Polygon polygon : mp.polygons) {
				iterPoints(polygon, fn);
			}
			return;
		}
		if (g instanceof GeometryCollection) {
			GeometryCollection coll = (GeometryCollection) g;
			for (Geometry gg : coll.geometries) {
				iterPoints(gg, fn);
			}
		}
	}

}
