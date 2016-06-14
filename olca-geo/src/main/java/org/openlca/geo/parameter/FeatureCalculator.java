package org.openlca.geo.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.openlca.geo.kml.FeatureType;
import org.openlca.geo.kml.KmlFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the parameter values for a given feature from intersecting shapes.
 */
class FeatureCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private DataStore dataStore;
	private Map<String, Double> defaults;

	public FeatureCalculator(DataStore dataStore, Map<String, Double> defaults) {
		this.dataStore = dataStore;
		this.defaults = defaults;
	}

	public Map<String, Double> calculate(KmlFeature feature,
			List<String> parameters, Map<String, Double> shares) {
		FeatureType type = feature.type;
		if (type == null
				|| type == FeatureType.EMPTY
				|| type == FeatureType.MULTI_GEOMETRY) {
			log.warn("cannot calculate parameter values for type {}", type);
			return Collections.emptyMap();
		}
		return calculate(parameters, shares);
	}

	private Map<String, Double> calculate(List<String> parameters,
			Map<String, Double> shares) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			Map<SimpleFeature, Double> _shares = new HashMap<>();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				if (shares.containsKey(shape.getID()))
					_shares.put(shape, shares.get(shape.getID()));
			}
			return fetchValues(_shares, parameters);
		} catch (Exception e) {
			log.error("failed to fetch parameters for feature", e);
			return null;
		}
	}

	private Map<String, Double> fetchValues(Map<SimpleFeature, Double> shares,
			List<String> params) {
		Map<String, Double> results = new HashMap<>();
		for (SimpleFeature feature : shares.keySet()) {
			Double share = shares.get(feature);
			if (share == null)
				continue;
			Map<String, Double> vals = featureValues(feature, params);
			for (String param : params) {
				Double v = vals.get(param);
				if (v == null)
					continue;
				double value = share * v;
				Double total = results.get(param);
				if (total == null) {
					results.put(param, value);
				} else {
					results.put(param, total + value);
				}
			}
		}
		return results;
	}

	private Map<String, Double> featureValues(SimpleFeature feature,
			List<String> params) {
		if (feature == null)
			return defaults;
		Map<String, Double> map = new HashMap<>();
		for (String param : params) {
			Object obj = feature.getAttribute(param);
			if (!(obj instanceof Number))
				continue;
			Number number = (Number) obj;
			map.put(param, number.doubleValue());
		}
		return map;
	}

	private SimpleFeatureIterator getIterator() throws Exception {
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureCollection collection = dataStore.getFeatureSource(
				typeName).getFeatures();
		return collection.features();
	}

}
