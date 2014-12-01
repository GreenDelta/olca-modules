package org.openlca.geo.kml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class KmlFeature {

	private final String kml;
	private final Geometry geometry;
	private final FeatureType type;

	private KmlFeature(String kml, Geometry geometry, FeatureType type) {
		this.geometry = geometry;
		this.type = type;
		this.kml = kml;
	}

	public static KmlFeature empty() {
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
		List<Geometry> geometries = new ArrayList<Geometry>();
		for (Object obj : featureList) {
			SimpleFeature feature = (SimpleFeature) obj;
			Geometry geometry = (Geometry) feature.getAttribute("Geometry");
			geometries.add(geometry);
		}
		Geometry geometry = merge(geometries);
		FeatureType type = getType(geometry);
		return new KmlFeature(kml, geometry, type);
	}

	private static Geometry merge(List<Geometry> geometries) {
		if (geometries == null || geometries.size() == 0)
			return null;
		List<Geometry> toMerge = new ArrayList<Geometry>();
		for (Geometry geometry : geometries)
			toMerge.addAll(collectSingleGeometries(geometry));
		if (toMerge.size() == 1)
			return toMerge.get(0);
		GeometryFactory factory = new GeometryFactory();
		FeatureType type = getCollectionType(toMerge);
		if (type == FeatureType.LINE)
			return factory.createMultiLineString(GeometryFactory
					.toLineStringArray(toMerge));
		if (type == FeatureType.POINT)
			return factory.createMultiPoint(GeometryFactory
					.toPointArray(toMerge));
		if (type == FeatureType.POLYGON)
			return factory.createMultiPolygon(GeometryFactory
					.toPolygonArray(toMerge));
		// else mixed content
		return factory.createGeometryCollection(toMerge
				.toArray(new Geometry[toMerge.size()]));
	}

	private static FeatureType getCollectionType(List<Geometry> geometries) {
		FeatureType type = null;
		for (Geometry geometry : geometries)
			if (type == null) // first geometry type
				type = getType(geometry);
			else if (type == getType(geometry)) // same type as first
				continue;
			else
				// mixed type, return null
				return null;
		return type;
	}

	private static List<Geometry> collectSingleGeometries(Geometry geometry) {
		List<Geometry> collected = new ArrayList<Geometry>();
		if (isSingleGeometry(geometry))
			collected.add(geometry);
		else
			for (int n = 0; n < geometry.getNumGeometries(); n++)
				collected.addAll(collectSingleGeometries(geometry
						.getGeometryN(n)));
		return collected;
	}

	private static boolean isSingleGeometry(Geometry geometry) {
		FeatureType type = getType(geometry);
		if (type == FeatureType.MULTI_GEOMETRY)
			return false;
		return true;
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
		case "MultiLineString":
		case "MultiPoint":
		case "MultiPolygon":
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

}
