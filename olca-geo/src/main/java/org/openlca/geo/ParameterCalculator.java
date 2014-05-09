package org.openlca.geo;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore dataStore;

	public ParameterCalculator(DataStore dataStore) {
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
				return fetchLineValues(feature, parameters);
			case POLYGON:
				return fetchPolygonValues(feature, parameters);
			default:
				log.warn("cannot calculate parameter values for type {}",
						feature.getType());
				return Collections.emptyMap();
		}
	}

	private Map<String, Double> fetchLineValues(KmlFeature feature,
			List<String> parameters) {
		try {
			double totalLength = feature.getGeometry().getLength();
			if (totalLength == 0)
				return Collections.emptyMap();
			Map<SimpleFeature, Double> shares = new HashMap<>();
			SimpleFeatureIterator iterator = getIterator();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry shapeGeo = (Geometry) shape.getDefaultGeometry();
				Geometry featureGeo = feature.getGeometry();
				if (!featureGeo.crosses(shapeGeo))
					continue;
				double length = featureGeo.intersection(shapeGeo).getLength();
				shares.put(shape, length / totalLength);
			}
			return fetchValues(shares, parameters);
		} catch (Exception e) {
			log.error("failed to fetch line parameters", e);
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> fetchPolygonValues(KmlFeature feature,
			List<String> parameters) {
		try {
			double totalArea = feature.getGeometry().getArea();
			if (totalArea == 0)
				return Collections.emptyMap();
			Map<SimpleFeature, Double> shares = new HashMap<>();
			SimpleFeatureIterator iterator = getIterator();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry shapeGeo = (Geometry) shape.getDefaultGeometry();
				Geometry featureGeo = feature.getGeometry();
				if (!featureGeo.intersects(shapeGeo))
					continue;
				double area = featureGeo.intersection(shapeGeo).getArea();
				shares.put(shape, area / totalArea);
			}
			return fetchValues(shares, parameters);
		} catch (Exception e) {
			log.error("failed to fetch polygon parameters", e);
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> fetchValues(Map<SimpleFeature, Double> shares,
			List<String> parameters) {
		Map<String, Double> results = new HashMap<>();
		for(SimpleFeature feature : shares.keySet()) {
			Double share = shares.get(feature);
			if(share == null)
				continue;
			Map<String, Double> vals = fetchValues(feature, parameters);
			for(String param : parameters) {
				Double val = vals.get(param);
				if(val == null)
					continue;
				double featureResult = share * val;
				Double totalResult = results.get(param);
				if(totalResult == null)
					results.put(param, featureResult);
				else
					results.put(param, totalResult + featureResult);
			}
		}
		return results;
	}

	private Map<String, Double> fetchPointValues(KmlFeature feature,
			List<String> parameters) {
		try {
			SimpleFeatureIterator iterator = getIterator();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				Geometry geometry = (Geometry) shape.getDefaultGeometry();
				if (geometry.contains(feature.getGeometry()))
					return fetchValues(shape, parameters);
			}
			return Collections.emptyMap();
		} catch (Exception e) {
			log.error("failed to fetch point values", e);
			return Collections.emptyMap();
		}
	}

	private SimpleFeatureIterator getIterator() throws Exception {
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureCollection collection = dataStore
				.getFeatureSource(typeName)
				.getFeatures();
		return collection.features();
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
}
