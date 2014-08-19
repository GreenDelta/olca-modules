package org.openlca.geo;

import java.io.StringReader;
import java.util.List;

import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.openlca.geo.multi.JakToGeotools;
import org.openlca.geo.multi.MultiGeometryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class KmlFeature {

	private final String kml;
	private final Geometry geometry;
	private final FeatureType type;

	private KmlFeature(String kml, Geometry geometry, FeatureType type) {
		this.geometry = geometry;
		this.type = type;
		this.kml = kml;
	}

	private static KmlFeature empty() {
		return new KmlFeature(null, null, FeatureType.EMPTY);
	}

	public static KmlFeature parse(String kml) throws Exception {
		KMLConfiguration configuration = new KMLConfiguration();
		Parser parser = new Parser(configuration);
		StringReader reader = new StringReader(kml);
		SimpleFeature root = (SimpleFeature) parser.parse(reader);
		Object featureObj = root.getAttribute("Feature");
		if (!(featureObj instanceof List))
			return empty();
		List<?> featureList = (List<?>) featureObj;
		if (featureList.isEmpty())
			return empty();
		SimpleFeature feature = (SimpleFeature) featureList.get(0);
		Geometry geometry = (Geometry) feature.getDefaultGeometry();
		FeatureType type = getType(geometry);
		// parse doesn't parse multi geometries correct
		// parse again with different api
		if (type == FeatureType.MULTI_GEOMETRY)
			geometry = parseMultiGeometry(kml);
		return new KmlFeature(kml, geometry, type);
	}

	private static Geometry parseMultiGeometry(String kml) {
		MultiGeometryParser parser = new MultiGeometryParser();
		return JakToGeotools.convert(parser.parse(kml));
	}
	
	private static FeatureType getType(Geometry geometry) {
		if (geometry == null)
			return FeatureType.EMPTY;
		String typeString = geometry.getGeometryType();
		if (typeString == null)
			return FeatureType.EMPTY;
		switch (typeString) {
		case "LineString":
			return FeatureType.LINE;
		case "Point":
			return FeatureType.POINT;
		case "Polygon":
			return FeatureType.POLYGON;
		case "GeometryCollection":
			return FeatureType.MULTI_GEOMETRY;
		default:
			Logger log = LoggerFactory.getLogger(KmlFeature.class);
			log.warn("unknown geometry {}; set type as empty", typeString);
			return null;
		}
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public FeatureType getType() {
		return type;
	}

	public String getKml() {
		return kml;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof KmlFeature))
			return false;
		KmlFeature other = (KmlFeature) obj;
		if (this.geometry == null)
			return other.geometry == null;
		else
			return other.geometry != null
					&& this.geometry.equals(other.geometry);
	}

}
