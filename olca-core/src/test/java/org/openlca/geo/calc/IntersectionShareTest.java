package org.openlca.geo.calc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openlca.geo.geojson.Feature;
import org.openlca.geo.geojson.Point;

public class IntersectionShareTest {

	@Test
	public void testMakeRelative() {
		var g =JTS.fromGeoJSON(new Point());
		var shares = IntersectionShare.makeRelative(List.of(
				IntersectionShare.of(new Feature(), g, 2.0),
				IntersectionShare.of(new Feature(), g, 10.0),
				IntersectionShare.of(new Feature(), g, 5.0)));
		assertEquals(0.2, shares.get(0).value(), 1e-16);
		assertEquals(1.0, shares.get(1).value(), 1e-16);
		assertEquals(0.5, shares.get(2).value(), 1e-16);
	}

}
