package org.openlca.geo.kml;

public class KmlTests {

	public static KmlFeature parse(String kml) {
		try {
			return KmlFeature.parse(kml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
