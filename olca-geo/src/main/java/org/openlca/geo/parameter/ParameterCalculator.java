package org.openlca.geo.parameter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.openlca.core.model.Parameter;
import org.openlca.geo.kml.FeatureType;
import org.openlca.geo.kml.KmlFeature;
import org.openlca.geo.kml.LocationKml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterCalculator implements Closeable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, List<String>> groups;
	private Map<String, DataStore> stores;
	private Map<String, Double> defaults;
	private ParameterCache cache;

	public ParameterCalculator(List<Parameter> parameters, ShapeFileFolder folder) {
		groups = groupByShapeFile(parameters);
		stores = openStores(groups.keySet(), folder);
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
		if (groups.isEmpty())
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
		for (String shapeFile : groups.keySet()) {
			Map<String, Double> shares = loadOrCalculateShares(locationId,
					feature, shapeFile);
			log.debug("Calculating parameters for location {}", locationId);
			DataStore store = stores.get(shapeFile);
			FeatureCalculator calculator = new FeatureCalculator(store, defaults);
			List<String> group = groups.get(shapeFile);
			Map<String, Double> result = calculator.calculate(feature, group, shares);
			if (result != null) {
				parameterMap.putAll(result);
			}
		}
		fillZeros(parameterMap);
		return parameterMap;
	}

	private Map<String, Double> loadOrCalculateShares(long locationId,
			KmlFeature feature, String shapeFile) {
		Map<String, Double> result = cache.load(locationId, shapeFile);
		if (result != null)
			return result;
		DataStore store = stores.get(shapeFile);
		IntersectionsCalculator calculator = new IntersectionsCalculator(store);
		log.debug("Calculating shares for location " + locationId);
		result = calculator.calculate(feature);
		cache.save(locationId, shapeFile, result);
		return result != null ? result : new HashMap<String, Double>();
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
