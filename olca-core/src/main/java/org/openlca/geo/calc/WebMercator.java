package org.openlca.geo.calc;

import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.Geometry;
import org.openlca.geo.geojson.Point;

/**
 * Implementation of the Web Mercator projection.
 */
public class WebMercator extends Projection {

	private final int zoom;

	public WebMercator(int zoom) {
		this.zoom = zoom;
	}

	/**
	 * Projects a WGS 84 (longitude, latitude)-point to a (x,y)- pixel
	 * coordinate. It directly mutates the coordinates of the point.
	 */
	public static void apply(Point p, int zoom) {
		if (p == null)
			return;
		double lon = p.x;
		if (lon < -180) {
			lon = -180;
		} else if (lon > 180) {
			lon = 180;
		}
		double lat = p.y;
		if (lat < -85.0511) {
			lat = -85.0511;
		} else if (lat > 85.0511) {
			lat = 85.0511;
		}

		lon *= Math.PI / 180;
		lat *= Math.PI / 180;
		double scale = (256 / (2 * Math.PI)) * Math.pow(2, zoom);
		p.x = scale * (lon + Math.PI);
		p.y = scale * (Math.PI - Math.log(Math.tan(Math.PI / 4 + lat / 2)));
	}

	/**
	 * The inverse operation of project. Calculates a WGS 84 (longitude,
	 * latitude)-point from a pixel coordinate. It directly mutates the
	 * given point.
	 */
	public static void inverse(Point p, int zoom) {
		if (p == null)
			return;
		double scale = (256 / (2 * Math.PI)) * Math.pow(2, zoom);
		p.x = (p.x / scale) - Math.PI;
		p.y = 2 * Math.atan(Math.exp(Math.PI - p.y / scale)) - Math.PI / 2;
		p.x *= 180 / Math.PI;
		p.y *= 180 / Math.PI;
	}

	public static Geometry project(Geometry geometry, int zoom) {
		if (geometry == null)
			return null;
		Geometry g = geometry.copy();
		new WebMercator(zoom).project(g);
		return g;
	}

	public static Feature project(Feature feature, int zoom) {
		if (feature == null)
			return null;
		Feature f = feature.copy();
		if (f.geometry != null) {
			WebMercator proj = new WebMercator(zoom);
			proj.project(f.geometry);
		}
		return f;
	}

	public static FeatureCollection project(FeatureCollection coll, int zoom) {
		if (coll == null)
			return null;
		FeatureCollection c = coll.copy();
		WebMercator proj = new WebMercator(zoom);
		for (Feature f : c.features) {
			proj.project(f.geometry);
		}
		return c;
	}

	@Override
	protected void apply(Point point) {
		apply(point, zoom);
	}

	@Override
	protected void inverse(Point point) {
		inverse(point, zoom);
	}

}
