package org.openlca.geo.parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactMethod.ParameterMean;
import org.openlca.geo.kml.FeatureType;
import org.openlca.geo.kml.KmlFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the parameter values for a given feature from intersecting shapes.
 */
class FeatureCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final DataStore dataStore;
	private final Map<String, Double> defaults;
	private final ImpactMethod.ParameterMean meanFn;

	public FeatureCalculator(DataStore dataStore,
			Map<String, Double> defaults, ParameterMean meanFn) {
		this.dataStore = dataStore;
		this.defaults = defaults;
		this.meanFn = meanFn;
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
		Map<String, Double> parameterResults = new HashMap<>();
		for (String param : params) {
			ValSet vals = new ValSet(param);
			for (Entry<SimpleFeature, Double> entry : shares.entrySet()) {
				Double val = getValue(entry.getKey(), param);
				Double share = entry.getValue();
				vals.add(val, share);
			}
			parameterResults.put(param, vals.getValue());
		}
		return parameterResults;
	}

	private Double getValue(SimpleFeature shape, String param) {
		if (shape == null || param == null)
			return null;
		Object obj = shape.getAttribute(param);
		if (!(obj instanceof Number))
			return null;
		Number number = (Number) obj;
		double val = number.doubleValue();
		return Val.isNaN(val) ? null : val;
	}

	private SimpleFeatureIterator getIterator() throws Exception {
		String typeName = dataStore.getTypeNames()[0];
		SimpleFeatureCollection collection = dataStore.getFeatureSource(
				typeName).getFeatures();
		return collection.features();
	}

	/**
	 * Set of values and shares of a parameter. A value of null means 'not
	 * available'.
	 */
	private class ValSet {

		String parameter;

		ArrayList<Double> values = new ArrayList<>();
		ArrayList<Double> shares = new ArrayList<>();
		int nanCount = 0;

		ValSet(String parameter) {
			this.parameter = parameter;
		}

		void add(Double value, Double share) {
			values.add(value);
			shares.add(share);
			if (value == null) {
				nanCount++;
			}
		}

		double getValue() {
			if (nanCount == values.size()) {
				Double v = defaults.get(parameter);
				return v == null ? 0 : v;
			}
			if (meanFn == ParameterMean.ARITHMETIC_MEAN)
				return mean();
			else
				return weightedMean(); // is also the default
		}

		private double mean() {
			double total = 0;
			double n = 0;
			for (Double val : values) {
				if (val == null)
					continue;
				n += 1.0;
				total += val;
			}
			return n == 0 ? 0 : total / n;
		}

		private double weightedMean() {
			double shareSum = 0;
			double total = 0;
			for (int i = 0; i < values.size(); i++) {
				Double val = values.get(i);
				if (val == null)
					continue;
				Double s = shares.get(i);
				double share = s == null ? 0 : s;
				total += val * share;
				shareSum += share;
			}
			if (nanCount == 0)
				return total;
			return shareSum == 0 ? 0 : total / shareSum;
		}
	}
}
