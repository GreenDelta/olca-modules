package org.openlca.geo.calc;

import org.junit.Test;
import org.openlca.geo.geojson.Geometry;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.Point;

public class IntersectionTest {

	/**
	 * Two lines can intersect in a point.
	 */
	@Test
	public void testLines() {

		LineString a = new LineString();
		a.points.add(new Point(20, 50));
		a.points.add(new Point(80, 60));

		LineString b = new LineString();
		b.points.add(new Point(35, 60));
		b.points.add(new Point(85, 40));

		Geometry g = Intersection.of(a, b);
		System.out.println(g);

	}

}
