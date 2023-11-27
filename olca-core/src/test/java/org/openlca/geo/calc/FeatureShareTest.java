package org.openlca.geo.calc;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.geo.geojson.Feature;

import java.util.List;

public class FeatureShareTest {

	@Test
	public void testMakeRelative() {
		var shares = FeatureShare.makeRelative(List.of(
				FeatureShare.of(new Feature(), 2.0),
				FeatureShare.of(new Feature(), 10.0),
				FeatureShare.of(new Feature(), 5.0)));
		assertEquals(0.2, shares.get(0).value(), 1e-16);
		assertEquals(1.0, shares.get(1).value(), 1e-16);
		assertEquals(0.5, shares.get(2).value(), 1e-16);
	}

}
