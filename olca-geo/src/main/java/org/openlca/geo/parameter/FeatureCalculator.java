package org.openlca.geo.parameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.openlca.geo.kml.KmlFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FeatureCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore dataStore;

	public FeatureCalculator(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public Map<String, Double> calculate(KmlFeature feature,
			List<String> parameters, Map<String, Double> defaults,
			Map<String, Double> shares) {
		if (feature.getType() == null)
			return Collections.emptyMap();
		switch (feature.getType()) {
		case POINT:
			return fetchPointValues(feature, parameters, shares);
		case LINE:
			return fetchValues(feature, parameters, defaults, shares);
		case POLYGON:
		case MULTI_GEOMETRY:
			return fetchValues(feature, parameters, defaults, shares);
		default:
			log.warn("cannot calculate parameter values for type {}",
					feature.getType());
			return Collections.emptyMap();
		}
	}

	private Map<String, Double> fetchPointValues(KmlFeature feature,
			List<String> parameters, Map<String, Double> shares) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				if (shares.containsKey(shape.getID()))
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
			List<String> parameters, Map<String, Double> defaults,
			Map<String, Double> shares) {
		try (SimpleFeatureIterator iterator = getIterator()) {
			Map<SimpleFeature, Double> _shares = new HashMap<>();
			while (iterator.hasNext()) {
				SimpleFeature shape = iterator.next();
				if (shares.containsKey(shape.getID()))
					_shares.put(shape, shares.get(shape.getID()));
			}
			return fetchValues(_shares, parameters, defaults);
		} catch (Exception e) {
			String type = feature.getType().name();
			log.error("failed to fetch parameters for feature type " + type, e);
			return null;
		}
	}

	private Map<String, Double> fetchValues(Map<SimpleFeature, Double> shares,
			List<String> parameters, Map<String, Double> defaults) {
		Map<String, Double> results = new HashMap<>();
		double totalShare = calculateTotalShare(shares);
		if (totalShare < 1)
			shares.put(null, (1 - totalShare));
		for (SimpleFeature feature : shares.keySet()) {
			Double share = shares.get(feature);
			if (share == null)
				continue;
			Map<String, Double> vals = defaults;
			if (feature != null)
				vals = fetchValues(feature, parameters);
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

	private double calculateTotalShare(Map<SimpleFeature, Double> shares) {
		double share = 0;
		for (Entry<SimpleFeature, Double> entry : shares.entrySet())
			if (entry.getValue() != null)
				share += entry.getValue();
		return share;
	}

	private SimpleFeatureIterator getIterator() throws Exception {
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureCollection collection = dataStore.getFeatureSource(
				typeName).getFeatures();
		return collection.features();
	}

}
