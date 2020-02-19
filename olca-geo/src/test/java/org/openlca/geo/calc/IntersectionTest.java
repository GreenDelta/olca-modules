package org.openlca.geo.calc;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.FeatureCollection;
import org.openlca.geo.geojson.Geometry;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.Point;
import org.openlca.util.Pair;

public class IntersectionTest {

	@Test
	public void testLineIntersection() {
		LineString a = new LineString();
		a.points.add(new Point(0, 0));
		a.points.add(new Point(10, 10));
		LineString b = new LineString();
		b.points.add(new Point(0, 10));
		b.points.add(new Point(10, 0));
		IntersectionCalculator calc = IntersectionCalculator.on(
				FeatureCollection.of(a));
		List<Pair<Feature, Geometry>> r = calc.calculate(b);
		Assert.assertEquals(1, r.size());
		Geometry g = r.get(0).second;
		Assert.assertTrue(g instanceof Point);
		Point p = (Point) g;
		Assert.assertEquals(5.0, p.x, 1e-16);
		Assert.assertEquals(5.0, p.y, 1e-16);
	}

	@Test
	public void testParallelLines() {
		LineString a = new LineString();
		a.points.add(new Point(0, 0));
		a.points.add(new Point(10, 0));
		LineString b = new LineString();
		b.points.add(new Point(0, 10));
		b.points.add(new Point(10, 10));
		IntersectionCalculator calc = IntersectionCalculator.on(
				FeatureCollection.of(a));
		List<Pair<Feature, Geometry>> r = calc.calculate(b);
		Assert.assertTrue(r.isEmpty());
	}

	@Test
	public void testLineProjection() {

		// TODO: test line projections
		LineString a = new LineString();
		a.points.add(new Point(20, 50));
		a.points.add(new Point(80, 60));

		LineString b = new LineString();
		b.points.add(new Point(35, 60));
		b.points.add(new Point(85, 40));
	}



}
