package org.openlca.geo.geojson;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class MessagePackTest {

	@Test
	public void testEmptyCollection() {
		FeatureCollection coll = new FeatureCollection();
		FeatureCollection clone = MsgPack.unpack(MsgPack.pack(coll));
		Assert.assertNotSame(coll, clone);
		Assert.assertTrue(clone.features.isEmpty());
	}

	@Test
	public void testEmptyFeature() {
		FeatureCollection coll = new FeatureCollection();
		coll.features.add(new Feature());
		FeatureCollection clone = MsgPack.unpack(MsgPack.pack(coll));
		Assert.assertNotSame(coll, clone);
		Assert.assertEquals(1, clone.features.size());
		Assert.assertNull(clone.features.get(0).geometry);
	}

	@Test
	public void testPointFeature() {
		FeatureCollection coll = new FeatureCollection();
		Feature feature = new Feature();
		feature.geometry = new Point(13.4, 54.2);
		coll.features.add(feature);
		FeatureCollection clone = MsgPack.unpack(MsgPack.pack(coll));

		Point point = (Point) clone.features.get(0).geometry;
		Assert.assertEquals(13.4, point.x, 1e-16);
		Assert.assertEquals(54.2, point.y, 1e-16);
	}

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
		byte[] data = MsgPack.pack(coll);
		FeatureCollection clone = MsgPack.unpack(data);

		Feature feature = clone.features.get(0);
		List<Point> points = ((MultiPoint) feature.geometry).points;
		Assert.assertEquals(100_000, points.size());
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			Assert.assertEquals(i, (int) p.x);
			Assert.assertEquals(i, (int) p.y);
		}
	}

	@Test
	public void testGz() {
		Point point = new Point(42, 24);
		byte[] data = MsgPack.packgz(FeatureCollection.of(point));
		FeatureCollection coll = MsgPack.unpackgz(data);
		Point clone = (Point) coll.features.get(0).geometry;
		Assert.assertEquals(point.x, clone.x, 1e-16);
		Assert.assertEquals(point.y, clone.y, 1e-16);
	}
}
