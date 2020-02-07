package org.openlca.geo.geojson;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the object serialization of a feature collection with Kryo:
 * <p>
 * https://github.com/EsotericSoftware/kryo
 */
public class KryoTest {

	@Test
	public void testMultiPoint() {

		// create 100_000 points
		MultiPoint multiPoint = new MultiPoint();
		for (int i = 0; i < 100_000; i++) {
			Point point = new Point();
			point.x = i;
			point.y = i;
			multiPoint.points.add(point);
		}

		FeatureCollection coll = FeatureCollection.of(multiPoint);
		byte[] data = GeoJSON.toKryo(coll);
		FeatureCollection clone = GeoJSON.fromKryo(data);

		Feature feature = clone.features.get(0);
		List<Point> points = ((MultiPoint) feature.geometry).points;
		Assert.assertEquals(100_000, points.size());
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			Assert.assertEquals(i, (int) p.x);
			Assert.assertEquals(i, (int) p.y);
		}
	}
}
