package org.openlca.geo.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

class IntersectionsCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore dataStore;

	public IntersectionsCalculator(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public Map<String, Double> calculate(KmlFeature feature, List<String> params) {
		if (feature.getType() == null)
			return Collections.emptyMap();
		Geometry geo = feature.getGeometry();
		switch (feature.getType()) {
		case POINT:
			return calculatePoint(geo, params, 1d);
		case MULTI_POINT:
			return calculateMulti((MultiPoint) geo, params);
		case LINE:
		case MULTI_LINE:
			return calculate(geo, params, new LineStringValueFetch());
		case POLYGON:
		case MULTI_POLYGON:
			return calculate(geo, params, new PolygonValueFetch());
		default:
			log.warn("cannot calculate shares for type {}", feature.getType());
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> calculateMulti(MultiPoint featureGeo,
			List<String> parameters) {
		Map<String, Double> result = new HashMap<>();
		int length = featureGeo.getNumGeometries();
		for (int i = 0; i < length; i++) {
			Geometry next = featureGeo.getGeometryN(i);
			result.putAll(calculatePoint(next, parameters, 1 / length));
		}
		return result;
	}

	private Map<String, Double> calculatePoint(Geometry featureGeo,
			List<String> parameters, double share) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry geometry = (Geometry) shape.getDefaultGeometry();
				if (geometry instanceof Point) {
					if (geometry.equalsExact(featureGeo, 1e-6))
						return Collections.singletonMap(shape.getID(), share);
				} else if (geometry.contains(featureGeo))
					return Collections.singletonMap(shape.getID(), share);
			}
			return Collections.emptyMap();
		} catch (Exception e) {
			log.error("failed to fetch point values", e);
			return null;
		}
	}

	private Map<String, Double> calculate(Geometry featureGeo,
			List<String> parameters, ValueFetch valueFetch) {
		double totalValue = valueFetch.fetchTotal(featureGeo);
		double total = 0;
		if (totalValue == 0)
			return Collections.emptyMap();
		try (SimpleFeatureIterator iterator = getIterator()) {
			Map<String, Double> shares = new HashMap<>();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry shapeGeo = (Geometry) shape.getDefaultGeometry();
				if (valueFetch.skip(featureGeo, shapeGeo))
					continue;
				double value = valueFetch.fetchSingle(featureGeo, shapeGeo);
				shares.put(shape.getID(), value / totalValue);
				total += value;
				if (total >= totalValue) // >= because of float representation
											// (might be 1.0000000002 e.g.)
					// found all intersections (per definition shape files do
					// not contain overlapping features)
					break;
			}
			return shares;
		} catch (Exception e) {
			log.error("failed to fetch parameters for feature", e);
			return null;
		}
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
				log.warn(
						"Topology exception in feature calculation, reducing precision of original model",
						e);
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
