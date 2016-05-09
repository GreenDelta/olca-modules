package org.openlca.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Geometries {

	private static final String POINT_KML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://earth.google.com/kml/2.1\"><Folder><Placemark><Point><coordinates>#{longitude},#{latitude}</coordinates></Point></Placemark></Folder></kml>";

	public static String pointToKml(double latitude, double longitude) {
		String kml = replace(POINT_KML, "latitude", latitude);
		return replace(kml, "longitude", longitude);
	}

	public static byte[] kmlToKmz(String kml) {
		if (kml == null)
			return null;
		if (kml.isEmpty())
			return null;
		try {
			return BinUtils.zip(kml.getBytes("utf-8"));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Geometries.class);
			log.error("failed to zip KML", e);
			return null;
		}
	}

	private static String replace(String string, String var, double value) {
		return string.replace("#{" + var + "}", Double.toString(value));
	}

}
