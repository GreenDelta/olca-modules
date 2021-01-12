package org.openlca.geo.calc;

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

		var theta = lat;
		var piSinLat = Math.PI * Math.sin(lat);
		int i = 0;
		while (i < 1000) {
			i++;
			var dividend = 2 * theta + Math.sin(2 * theta) - piSinLat;
			var divisor = 2 + 2 * Math.cos(2 * theta);
			var next = theta - dividend / divisor;
			var doBreak = Math.abs(theta - next) < 1e-12;
			theta = next;
			if (doBreak)
				break;
		}

		p.x = R * 2 * Math.sqrt(2) * lon * Math.cos(theta) / Math.PI;
		p.y = R * Math.sqrt(2) * Math.sin(theta);
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
	protected void inverse(Point point) {

	}
}
