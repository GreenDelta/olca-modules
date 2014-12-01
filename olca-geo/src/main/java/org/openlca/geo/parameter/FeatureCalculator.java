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

class FeatureCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore dataStore;

	public FeatureCalculator(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public Map<String, Double> calculate(KmlFeature feature,
			List<String> parameters) {
		if (feature.getType() == null)
			return Collections.emptyMap();
		switch (feature.getType()) {
		case POINT:
			return fetchPointValues(feature, parameters);
		case LINE:
			return fetchValues(feature, parameters, new LineStringValueFetch());
		case POLYGON:
		case MULTI_GEOMETRY:
			return fetchValues(feature, parameters, new PolygonValueFetch());
		default:
			log.warn("cannot calculate parameter values for type {}",
					feature.getType());
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> fetchPointValues(KmlFeature feature,
			List<String> parameters) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry geometry = (Geometry) shape.getDefaultGeometry();
				if (geometry.contains(feature.getGeometry()))
					return fetchValues(shape, parameters);
			}
			return Collections.emptyMap();
		} catch (Exception e) {
			log.error("failed to fetch point values", e);
			return null;
		}
	}

	private Map<String, Double> fetchValues(SimpleFeature feature,
			List<String> parameters) {
		Map<String, Double> map = new HashMap<>();
		for (String param : parameters) {
			Object obj = feature.getAttribute(param);
			if (!(obj instanceof Number))
				continue;
			Number number = (Number) obj;
			map.put(param, number.doubleValue());
		}
		return map;
	}

	private Map<String, Double> fetchValues(KmlFeature feature,
			List<String> parameters, ValueFetch valueFetch) {
		double totalValue = valueFetch.fetchTotal(feature);
		if (totalValue == 0)
			return Collections.emptyMap();
		try (SimpleFeatureIterator iterator = getIterator()) {
			Map<SimpleFeature, Double> shares = new HashMap<>();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry shapeGeo = (Geometry) shape.getDefaultGeometry();
				Geometry featureGeo = feature.getGeometry();
				if (valueFetch.skip(featureGeo, shapeGeo))
					continue;
				double value = valueFetch.fetchSingle(featureGeo, shapeGeo);
				shares.put(shape, value / totalValue);
			}
			return fetchValues(shares, parameters);
		} catch (Exception e) {
			String type = feature.getType().name();
			log.error("failed to fetch parameters for feature type " + type, e);
			return null;
		}
	}

	private Map<String, Double> fetchValues(Map<SimpleFeature, Double> shares,
			List<String> parameters) {
		Map<String, Double> results = new HashMap<>();
		for (SimpleFeature feature : shares.keySet()) {
			Double share = shares.get(feature);
			if (share == null)
				continue;
			Map<String, Double> vals = fetchValues(feature, parameters);
			for (String param : parameters) {
				Double val = vals.get(param);
				if (val == null)
					continue;
				double featureResult = share * val;
				Double totalResult = results.get(param);
				if (totalResult == null)
					results.put(param, featureResult);
				else
					results.put(param, totalResult + featureResult);
			}
		}
		return results;
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
			return feature.intersection(shape).getArea();
		}

		@Override
		public boolean skip(Geometry feature, Geometry shape) {
			return !feature.intersects(shape);
		}

	}

}
