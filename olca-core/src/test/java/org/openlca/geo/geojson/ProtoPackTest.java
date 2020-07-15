package org.openlca.geo.geojson;

import org.junit.Assert;
import org.junit.Test;

public class ProtoPackTest {

	@Test
	public void testEmptyCollection() {
		var coll = new FeatureCollection();
		var clone = ProtoPack.unpack(ProtoPack.pack(coll));
		Assert.assertNotSame(coll, clone);
		Assert.assertTrue(clone.features.isEmpty());
	}

	@Test
	public void testEmptyFeature() {
		var coll = new FeatureCollection();
		coll.features.add(new Feature());
		var clone = ProtoPack.unpack(ProtoPack.pack(coll));
		Assert.assertNotSame(coll, clone);
		Assert.assertEquals(1, clone.features.size());
		Assert.assertNull(clone.features.get(0).geometry);
	}

	@Test
	public void testPointFeature() {
		var coll = new FeatureCollection();
		var feature = new Feature();
		feature.geometry = new Point(13.4, 54.2);
		coll.features.add(feature);
		var clone = ProtoPack.unpack(ProtoPack.pack(coll));

		var point = (Point) clone.features.get(0).geometry;
		Assert.assertEquals(13.4, point.x, 1e-16);
		Assert.assertEquals(54.2, point.y, 1e-16);
	}

	@Test
	public void testMultiPoint() {

		// create 100_000 points
		var multiPoint = new MultiPoint();
		for (int i = 0; i < 100_000; i++) {
			var point = new Point();
			point.x = i;
			point.y = i;
			multiPoint.points.add(point);
		}

		var coll = FeatureCollection.of(multiPoint);
		byte[] data = ProtoPack.pack(coll);
		var clone = ProtoPack.unpack(data);

		var feature = clone.features.get(0);
		var points = ((MultiPoint) feature.geometry).points;
		Assert.assertEquals(100_000, points.size());
		for (int i = 0; i < points.size(); i++) {
			var p = points.get(i);
			Assert.assertEquals(i, (int) p.x);
			Assert.assertEquals(i, (int) p.y);
		}
	}

	@Test
	public void testGz() {
		var point = new Point(42, 24);
		byte[] data = ProtoPack.packgz(FeatureCollection.of(point));
		var coll = ProtoPack.unpackgz(data);
		var clone = (Point) coll.features.get(0).geometry;
		Assert.assertEquals(point.x, clone.x, 1e-16);
		Assert.assertEquals(point.y, clone.y, 1e-16);
	}
}
