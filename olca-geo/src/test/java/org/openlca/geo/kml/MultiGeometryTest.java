package org.openlca.geo.kml;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.Tests;

import com.vividsolutions.jts.geom.Geometry;

public class MultiGeometryTest {

	private static final double[] COORDS_NO_INTERSECT = { 6.558837890625187,
			26.017297563851848, -3.922119140624727, 17.01476753055841,
			-0.4943847656247573, 16.699340234594917, 9.744873046874847,
			25.18505888358141, 6.558837890625187, 26.017297563851848 };

	private static final double[] COORDS_OVERLAP_UPPER_POLYGON = {
			-4.7131347656247655, 26.07652055985743, -4.449462890624783,
			16.909683615559448, 6.4050292968752425, 26.17515899017839,
			-4.7131347656247655, 26.07652055985743 };

	private static final double[] COORDS_OVERLAP_BOTH_POLYGONS = {
			0.25268554687523986, 26.332806922898285, 0.3625488281252508,
			15.98245352297385, 8.756103515625227, 16.235772090430007,
			8.4704589843752, 26.352497858154532, 0.25268554687523986,
			26.332806922898285 };

	@Test
	public void noIntersection() throws Exception {
		String kml = Tests.getKml("multipolygon.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Geometry multi = feature.geometry;
		Geometry noIntersect = Tests.createPolygon(COORDS_NO_INTERSECT);
		double area = multi.intersection(noIntersect).getArea();
		Assert.assertEquals(0d, area, 0d);
	}

	@Test
	public void intersectUpperPolygon() throws Exception {
		String kml = Tests.getKml("multipolygon.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Geometry multi = feature.geometry;
		Geometry upper = Tests.createPolygon(COORDS_OVERLAP_UPPER_POLYGON);
		double area = multi.intersection(upper).getArea();
		Assert.assertNotEquals(0d, area, 0d);
	}

	@Test
	public void intersectBothPolygons() throws Exception {
		String kml = Tests.getKml("multipolygon.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Geometry multi = feature.geometry;
		Geometry both = Tests.createPolygon(COORDS_OVERLAP_BOTH_POLYGONS);
		double area = multi.intersection(both).getArea();
		Assert.assertNotEquals(0d, area, 0d);
	}

}
