package org.openlca.geo.parameter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.data.DataStore;
import org.openlca.core.model.ImpactMethod.ParameterMean;
import org.openlca.core.model.Parameter;
import org.openlca.geo.kml.FeatureType;
import org.openlca.geo.kml.KmlFeature;
import org.openlca.geo.kml.LocationKml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterCalculator implements Closeable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ParameterMean meanFn;
	private final Map<String, List<String>> parameters;
	private final Map<String, DataStore> stores;
	private final Map<String, Double> defaults;
	private final ParameterCache cache;

	public ParameterCalculator(List<Parameter> parameters,
			ShapeFileFolder folder, ParameterMean meanFn) {
		this.parameters = groupByShapeFile(parameters);
		this.meanFn = meanFn;
		stores = openStores(this.parameters.keySet(), folder);
		cache = new ParameterCache(folder);
		defaults = new HashMap<>();
		for (Parameter p : parameters) {
			defaults.put(p.getName(), p.getValue());
		}
	}

	@Override
	public void close() {
		for (DataStore dataStore : stores.values()) {
			dataStore.dispose();
		}
	}

	public ParameterSet calculate(List<LocationKml> kmlData) {
		ParameterSet set = new ParameterSet(defaults);
		if (parameters.isEmpty())
			return set;
		for (LocationKml data : kmlData) {
			KmlFeature feature = data.kmlFeature;
			if (feature == null || feature.type == FeatureType.EMPTY)
				continue;
			Map<String, Double> parameters = calculate(data.locationId, feature);
			set.put(data.locationId, parameters);
		}
		return set;
	}

	public Map<String, Double> calculate(long locationId, KmlFeature feature) {
		Map<String, Double> parameterMap = new HashMap<String, Double>();
		for (String shapeFile : parameters.keySet()) {
			Map<String, Double> shares = getShares(locationId, feature, shapeFile);
			log.debug("Calculating parameters for location {}", locationId);
			DataStore store = stores.get(shapeFile);
			FeatureCalculator calculator = new FeatureCalculator(store, defaults, meanFn);
			List<String> params = parameters.get(shapeFile);
			Map<String, Double> result = calculator.calculate(feature, params, shares);
			if (result != null) {
				parameterMap.putAll(result);
			}
		}
		fillZeros(parameterMap);
		return parameterMap;
	}

	/**
	 * Returns the intersection shares of the geometries in the shapefile for
	 * the given feature: geometry ID -> share
	 */
	private Map<String, Double> getShares(long locationId, KmlFeature feature,
			String shapeFile) {
		Map<String, Double> shares = cache.load(locationId, shapeFile);
		if (shares == null) {
			DataStore store = stores.get(shapeFile);
			IntersectionsCalculator calculator = new IntersectionsCalculator(store);
			log.debug("Calculating shares for location " + locationId);
			shares = calculator.calculate(feature);
			cache.save(locationId, shapeFile, shares);
		}
		HashMap<String, Double> r = new HashMap<>();
		for (Entry<String, Double> e : shares.entrySet()) {
			Double val = e.getValue();
			if (val == null || Val.isZero(val))
				continue;
			r.put(e.getKey(), val);
		}
		return r;
	}

	private void fillZeros(Map<String, Double> results) {
		for (String param : defaults.keySet()) {
			Double r = results.get(param);
			if (r == null) {
				results.put(param, 0d);
			}
		}
	}

	private Map<String, DataStore> openStores(Set<String> shapeFiles,
			ShapeFileFolder folder) {
		Map<String, DataStore> stores = new HashMap<>();
		for (String shapeFile : shapeFiles) {
			DataStore store = folder.openDataStore(shapeFile);
			stores.put(shapeFile, store);
		}
		return stores;
	}

	private Map<String, List<String>> groupByShapeFile(List<Parameter> params) {
		Map<String, List<String>> groups = new HashMap<>();
		for (Parameter param : params) {
			String shapeFile = param.getExternalSource();
			List<String> group = groups.get(shapeFile);
			if (group == null) {
				group = new ArrayList<>();
				groups.put(shapeFile, group);
			}
			group.add(param.getName());
		}
		return groups;
	}
}
