package org.openlca.geo.calc;

import org.openlca.core.model.Location;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.geo.geojson.Point;

public class Mollweide extends Projection {

	private final double R;

	public Mollweide(double r) {
		this.R = r;
	}

	public Mollweide() {
		this(1.0);
	}

	@Override
	protected void apply(Point p) {
		if (p == null)
			return;
		// see https://mathworld.wolfram.com/MollweideProjection.html
		// and https://en.wikipedia.org/wiki/Mollweide_projection
		var lon = longitudeOf(p);
		var lat = latitudeOf(p);
		double theta = thetaOf(lat);
		p.x = R * 2 * Math.sqrt(2) * lon * Math.cos(theta) / Math.PI;
		p.y = R * Math.sqrt(2) * Math.sin(theta);
	}

	private double thetaOf(double lat) {
		var theta = lat;
		var piSinLat = Math.PI * Math.sin(lat);
		int i = 0;
		while (i < 1000) {
			i++;
			var dividend = 2 * theta + Math.sin(2 * theta) - piSinLat;
			var divisor = 2 + 2 * Math.cos(2 * theta);
			if (divisor == 0)
				return lat;
			var next = theta - dividend / divisor;
			if(Math.abs(theta - next) < 1e-12)
				return next;
			theta = next;
		}
		return theta;
	}

	private double longitudeOf(Point p) {
		var lon = p.x;
		if (lon < -180) {
			lon = 180;
		} else if (lon > 180) {
			lon = 180;
		}
		return Math.toRadians(lon);
	}

	private double latitudeOf(Point p) {
		var lat = p.y;
		if (lat < -90) {
			lat = -90;
		} else if (lat > 90) {
			lat = 90;
		}
		return Math.toRadians(lat);
	}

	@Override
	protected void inverse(Point p) {
		if (p == null)
			return;
		var theta = Math.asin(p.y / (R * Math.sqrt(2)));
		var lat = Math.asin((2 * theta + Math.sin(2 * theta)) / Math.PI);
		var lon = Math.PI * p.x / (2 * R * Math.sqrt(2) * Math.cos(theta));
		p.x = Math.toDegrees(lon);
		p.y = Math.toDegrees(lat);
	}

	/**
	 * Calculates the area of the geometry of the given location in m2.
	 */
	public static double areaOf(Location loc) {
		if (loc == null || loc.geodata == null)
			return 0;
		var features = GeoJSON.unpack(loc.geodata);
		if (features.isEmpty())
			return 0;
		var feature = features.first();
		if (feature == null || feature.geometry == null)
			return 0;
		var projection = new Mollweide(6_378_137);
		projection.project(feature);
		var jts = JTS.fromGeoJSON(feature.geometry);
		return jts.getArea();
	}
}
