package org.openlca.geo;

import org.geotools.data.DataStore;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.model.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ParameterSet {

	private final Map<LongPair, Map<String, Double>> maps;
	private final Map<String, Double> defaults;

	private ParameterSet(Map<LongPair, Map<String, Double>> maps,
			Map<String, Double> defaults) {
		this.maps = maps;
		this.defaults = defaults;
	}

	Map<String, Double> get(LongPair processProduct) {
		Map<String, Double> map = maps.get(processProduct);
		if (map == null)
			return defaults;
		else
			return map;
	}

	static ParameterSet calculate(Map<LongPair, KmlFeature> features,
			List<Parameter> parameters, ShapeFileRepository repository) {
		Map<String, List<String>> groups = groupParameters(parameters);
		Map<String, DataStore> stores = openStores(groups.keySet(), repository);
		Map<String, Double> defaults = getDefaultValues(parameters);
		Map<LongPair, Map<String, Double>> maps = new HashMap<>();
		for (LongPair processProduct : features.keySet()) {
			KmlFeature feature = features.get(processProduct);
			if (feature == null || feature.getType() == FeatureType.EMPTY)
				continue;
			Map<String, Double> results = new HashMap<>();
			for (String shapeFile : groups.keySet()) {
				DataStore store = stores.get(shapeFile);
				ParameterCalculator calculator = new ParameterCalculator(store);
				List<String> group = groups.get(shapeFile);
				Map<String, Double> r = calculator.calculate(feature, group);
				results.putAll(r);
			}
			fillDefaults(results, defaults);
			maps.put(processProduct, results);
		}
		return new ParameterSet(maps, defaults);
	}

	private static void fillDefaults(Map<String, Double> results,
			Map<String, Double> defaults) {
		for (String param : defaults.keySet()) {
			Double r = results.get(param);
			if (r == null)
				results.put(param, defaults.get(param));
		}
	}

	private static Map<String, Double> getDefaultValues(List<Parameter>
			parameters) {
		Map<String, Double> defaultValues = new HashMap<>();
		for (Parameter parameter : parameters) {
			defaultValues.put(parameter.getName(), parameter.getValue());
		}
		return defaultValues;
	}

	private static Map<String, DataStore> openStores(Set<String> shapeFiles,
			ShapeFileRepository repository) {
		Map<String, DataStore> stores = new HashMap<>();
		for (String shapeFile : shapeFiles) {
			DataStore store = repository.openDataStore(shapeFile);
			stores.put(shapeFile, store);
		}
		return stores;
	}

	private static Map<String, List<String>> groupParameters(List<Parameter>
			parameters) {
		Map<String, List<String>> groups = new HashMap<>();
		for (Parameter param : parameters) {
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
