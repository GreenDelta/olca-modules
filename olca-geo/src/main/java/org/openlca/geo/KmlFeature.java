package org.openlca.geo;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;

public class KmlFeature {

	private final String kml;
	private final SimpleFeature feature;
	private final Geometry geometry;
	private final FeatureType type;

	private KmlFeature(String kml, SimpleFeature feature, Geometry geometry,
			FeatureType type) {
		this.feature = feature;
		this.geometry = geometry;
		this.type = type;
		this.kml = kml;
	}

	private static KmlFeature empty() {
		return new KmlFeature(null, null, null, FeatureType.EMPTY);
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
		return new KmlFeature(kml, feature, geometry, getType(geometry));
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
		default:
			Logger log = LoggerFactory.getLogger(KmlFeature.class);
			log.warn("unknown geometry {}; set type as empty", typeString);
			return null;
		}
	}

	public SimpleFeature getFeature() {
		return feature;
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
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(!(obj instanceof  KmlFeature))
			return false;
		KmlFeature other = (KmlFeature) obj;
		if(this.geometry == null)
			return other.geometry == null;
		else
			return other.geometry != null
					&& this.geometry.equals(other.geometry);
	}

}
