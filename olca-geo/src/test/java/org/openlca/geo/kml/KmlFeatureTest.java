package org.openlca.geo.kml;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.geo.Tests;

import com.vividsolutions.jts.geom.Coordinate;

public class KmlFeatureTest {

	@Test
	public void testLine() throws Exception {
		String kml = Tests.getKml("line.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Assert.assertEquals(FeatureType.LINE, feature.getType());
		Assert.assertTrue(feature.getGeometry().getLength() > 0);
		Assert.assertEquals(4, feature.getGeometry().getCoordinates().length);
	}

	@Test
	public void testPoint() throws Exception {
		String kml = Tests.getKml("point.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Assert.assertEquals(FeatureType.POINT, feature.getType());
		Coordinate coordinate = feature.getGeometry().getCoordinate();
		Assert.assertEquals(-104.37011718750457, coordinate.x, 1e-17);
		Assert.assertEquals(35.67514743608417, coordinate.y, 1e-17);
	}

	@Test
	public void testPolygon() throws Exception {
		String kml = Tests.getKml("polygon.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Assert.assertEquals(FeatureType.POLYGON, feature.getType());
		Assert.assertTrue(feature.getGeometry().getArea() > 0);
		Assert.assertEquals(6, feature.getGeometry().getCoordinates().length);
	}

	@Test
	public void testMultiPolygon() throws Exception {
		String kml = Tests.getKml("multipolygon.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Assert.assertEquals(FeatureType.MULTI_POLYGON, feature.getType());
		Assert.assertTrue(feature.getGeometry().getArea() > 0);
		Assert.assertEquals(2, feature.getGeometry().getNumGeometries());
		Assert.assertEquals(8, feature.getGeometry().getCoordinates().length);
	}

	@Test
	public void testMultiPlacemarks() throws Exception {
		String kml = Tests.getKml("multiplacemarks.kml");
		KmlFeature feature = KmlFeature.parse(kml);
		Assert.assertEquals(FeatureType.MULTI_POLYGON, feature.getType());
		Assert.assertTrue(feature.getGeometry().getArea() > 0);
		Assert.assertEquals(14, feature.getGeometry().getCoordinates().length);
	}

}
