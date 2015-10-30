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

	public Map<String, Double> calculate(KmlFeature feature,
			List<String> parameters) {
		if (feature.getType() == null)
			return Collections.emptyMap();
		switch (feature.getType()) {
		case POINT:
			return calculatePointShares(feature, parameters);
		case LINE:
			return calculateShares(feature, parameters,
					new LineStringValueFetch());
		case POLYGON:
		case MULTI_GEOMETRY:
			return calculateShares(feature, parameters, new PolygonValueFetch());
		default:
			log.warn("cannot calculate shares for type {}", feature.getType());
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> calculatePointShares(KmlFeature feature,
			List<String> parameters) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry geometry = (Geometry) shape.getDefaultGeometry();
				if (geometry instanceof Point) {
					if (geometry.equalsExact(feature.getGeometry(), 1e-6))
						return Collections.singletonMap(shape.getID(), 1d);
				} else if (geometry.contains(feature.getGeometry()))
					return Collections.singletonMap(shape.getID(), 1d);
			}
			return Collections.emptyMap();
		} catch (Exception e) {
			log.error("failed to fetch point values", e);
			return null;
		}
	}

	private Map<String, Double> calculateShares(KmlFeature feature,
			List<String> parameters, ValueFetch valueFetch) {
		double totalValue = valueFetch.fetchTotal(feature);
		double total = 0;
		if (totalValue == 0)
			return Collections.emptyMap();
		try (SimpleFeatureIterator iterator = getIterator()) {
			Map<String, Double> shares = new HashMap<>();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry shapeGeo = (Geometry) shape.getDefaultGeometry();
				Geometry featureGeo = feature.getGeometry();
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
			String type = feature.getType().name();
			log.error("failed to fetch parameters for feature type " + type, e);
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

		double fetchTotal(KmlFeature feature);

		double fetchSingle(Geometry feature, Geometry shape);

		boolean skip(Geometry feature, Geometry shape);
	}

	private class LineStringValueFetch implements ValueFetch {

		@Override
		public double fetchTotal(KmlFeature feature) {
			return feature.getGeometry().getLength();
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
		public double fetchTotal(KmlFeature feature) {
			return feature.getGeometry().getArea();
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
