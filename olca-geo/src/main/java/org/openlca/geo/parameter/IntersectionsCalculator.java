package org.openlca.geo.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.openlca.geo.kml.KmlFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

/**
 * Calculates the intersections between a KML feature of a location and the
 * respective shapes in an shape file. It returns a map which contains the IDs
 * of the intersected shapes and the respective shares of the shapes to the
 * total value (e.g. total intersected area). Note that the shares are related
 * to the intersection total and not to the total of the feature.
 */
class IntersectionsCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore dataStore;

	IntersectionsCalculator(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	Map<String, Double> calculate(KmlFeature feature) {
		if (feature.type == null)
			return Collections.emptyMap();
		Geometry geo = feature.geometry;
		switch (feature.type) {
		case POINT:
			String shapeId = findPointShape(geo);
			return shapeId == null ? Collections.emptyMap()
					: Collections.singletonMap(shapeId, 1d);
		case MULTI_POINT:
			return calculateMultiPoint((MultiPoint) geo);
		case LINE:
		case MULTI_LINE:
			return calculate(geo, new LineStringValueFetch());
		case POLYGON:
		case MULTI_POLYGON:
			return calculate(geo, new PolygonValueFetch());
		default:
			log.warn("cannot calculate shares for type {}", feature.type);
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> calculateMultiPoint(MultiPoint points) {
		Map<String, Double> result = new HashMap<>();
		int count = points.getNumGeometries();
		double total = 0;
		for (int i = 0; i < count; i++) {
			Geometry geo = points.getGeometryN(i);
			String shapeId = findPointShape(geo);
			if (shapeId == null)
				continue;
			Double val = result.get(shapeId);
			if (val == null) {
				result.put(shapeId, 1d);
			} else {
				result.put(shapeId, val + 1d);
			}
			total += 1d;
		}
		return makeRelative(result, total);
	}

	private String findPointShape(Geometry feature) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry geo = (Geometry) shape.getDefaultGeometry();
				if (geo instanceof Point && geo.equalsExact(feature, 1e-6))
					return shape.getID();
				else if (geo.contains(feature))
					return shape.getID();
			}
			return null;
		} catch (Exception e) {
			log.error("failed to fetch point values", e);
			return null;
		}
	}

	private Map<String, Double> calculate(Geometry featureGeo, ValueFetch fetch) {
		double featureTotal = fetch.fetchTotal(featureGeo);
		if (featureTotal == 0)
			return Collections.emptyMap();
		try (SimpleFeatureIterator iterator = getIterator()) {
			double total = 0;
			Map<String, Double> shares = new HashMap<>();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry shapeGeo = (Geometry) shape.getDefaultGeometry();
				if (shapeGeo == null) {
					log.warn("No default geometry found in shape "
							+ shape.getID() + " - Skipping");
					continue;
				}
				if (fetch.skip(featureGeo, shapeGeo))
					continue;
				double value = fetch.fetchSingle(featureGeo, shapeGeo);
				shares.put(shape.getID(), value);
				total += value;
				if (Math.abs(total - featureTotal) < 1e-16)
					break;
			}
			return makeRelative(shares, total);
		} catch (Exception e) {
			log.error("failed to fetch parameters for feature", e);
			return null;
		}
	}

	private Map<String, Double> makeRelative(Map<String, Double> shares,
			double total) {
		for (String id : shares.keySet()) {
			double val = shares.get(id);
			if (total == 0) {
				shares.put(id, 0d);
			} else {
				shares.put(id, val / total);
			}
		}
		return shares;
	}

	private SimpleFeatureIterator getIterator() throws Exception {
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureCollection collection = dataStore.getFeatureSource(
				typeName).getFeatures();
		return collection.features();
	}

	private interface ValueFetch {

		double fetchTotal(Geometry feature);

		double fetchSingle(Geometry feature, Geometry shape);

		boolean skip(Geometry feature, Geometry shape);
	}

	private class LineStringValueFetch implements ValueFetch {

		@Override
		public double fetchTotal(Geometry feature) {
			return feature.getLength();
		}

		@Override
		public double fetchSingle(Geometry feature, Geometry shape) {
			return feature.intersection(shape).getLength();
		}

		@Override
		public boolean skip(Geometry feature, Geometry shape) {
			return !feature.crosses(shape);
		}

	}

	private class PolygonValueFetch implements ValueFetch {

		@Override
		public double fetchTotal(Geometry feature) {
			return feature.getArea();
		}

		@Override
		public double fetchSingle(Geometry feature, Geometry shape) {
			try {
				return feature.intersection(shape).getArea();
			} catch (TopologyException e) {
				// see http://tsusiatsoftware.net/jts/jts-faq/jts-faq.html#D9
				log.warn("Topology exception in feature calculation, "
						+ "reducing precision of original model", e);
				feature = GeometryPrecisionReducer.reduce(feature,
						new PrecisionModel(PrecisionModel.FLOATING_SINGLE));
				return feature.intersection(shape).getArea();
			}
		}

		@Override
		public boolean skip(Geometry feature, Geometry shape) {
			return !feature.intersects(shape);
		}
	}
}
