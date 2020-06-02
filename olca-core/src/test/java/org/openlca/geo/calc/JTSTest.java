package org.openlca.geo.calc;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.openlca.geo.geojson.LineString;
import org.openlca.geo.geojson.MultiPoint;
import org.openlca.geo.geojson.Point;

public class JTSTest {

	@Test
	public void testDimension() {

		Geometry point = JTS.fromGeoJSON(new Point(13, 52));
		Assert.assertEquals(0, point.getDimension());
		Assert.assertEquals(1, point.getNumPoints());
		Assert.assertEquals(0, point.getLength(), 1e-16);
		Assert.assertEquals(0, point.getArea(), 1e-16);

		Geometry multiPoint = JTS.fromGeoJSON(
				new MultiPoint(Arrays.asList(
						new Point(13, 52),
						new Point(14, 56))));
		Assert.assertEquals(0, multiPoint.getDimension());
		Assert.assertEquals(2, multiPoint.getNumGeometries());
		Assert.assertEquals(0, multiPoint.getLength(), 1e-16);
		Assert.assertEquals(0, multiPoint.getArea(), 1e-16);

		Geometry line = JTS.fromGeoJSON(
				new LineString(Arrays.asList(
						new Point(13, 52),
						new Point(14, 56))));
		Assert.assertEquals(1, line.getDimension());
		Assert.assertEquals(1, line.getNumGeometries());
		Assert.assertEquals(Math.sqrt(17), line.getLength(), 1e-16);
		Assert.assertEquals(0, line.getArea(), 1e-16);
	}

}
