package org.openlca.geo;

import org.junit.Test;
import org.openlca.geo.multi.MultiPlacemarkMerger;

public class PlacemarkMergerTest {

	@Test
	public void testPlacemarkMerging() {
		MultiPlacemarkMerger merger = new MultiPlacemarkMerger();
		String kml = Tests.getKml("multiplacemarks.kml");
		merger.merge(kml);
	}

}
