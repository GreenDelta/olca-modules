package org.openlca.geo.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.openlca.core.model.Parameter;
import org.openlca.geo.kml.FeatureType;
import org.openlca.geo.kml.KmlFeature;

public class ParameterSetBuilder {

	private Map<String, List<String>> groups;
	private Map<String, DataStore> stores;
	private Map<String, Double> defaults;
	private ParameterRepository repository;

	public static ParameterSetBuilder createBuilder(List<Parameter> parameters,
			ShapeFileRepository shapeFileRepository,
			ParameterRepository parameterRepository) {
		ParameterSetBuilder builder = new ParameterSetBuilder();
		builder.groups = groupParameters(parameters);
		builder.stores = openStores(builder.groups.keySet(),
				shapeFileRepository);
		builder.defaults = getDefaultValues(parameters);
		builder.repository = parameterRepository;
		return builder;
	}

	private ParameterSetBuilder() {
		// hide constructor
	}

	public ParameterSet build(Collection<KmlFeature> features) {
		ParameterSet parameterSet = new ParameterSet(defaults);
		if (groups.isEmpty())
			return parameterSet;
		for (KmlFeature feature : features) {
			if (feature.getType() == FeatureType.EMPTY)
				continue;
			Map<String, Double> parameterMap = new HashMap<String, Double>();
			for (String shapeFile : groups.keySet())
				parameterMap.putAll(loadOrCalculate(feature, shapeFile));
			fillDefaults(parameterMap, defaults);
			parameterSet.put(feature, parameterMap);
		}
		return parameterSet;
	}

	private Map<String, Double> loadOrCalculate(KmlFeature feature,
			String shapeFile) {
		Map<String, Double> result = repository.load(feature, shapeFile);
		if (result != null)
			return result;
		DataStore store = stores.get(shapeFile);
		ParameterCalculator calculator = new ParameterCalculator(store);
		List<String> group = groups.get(shapeFile);
		result = calculator.calculate(feature, group);
		repository.save(feature, shapeFile, result);
		return result != null ? result : new HashMap<String, Double>();
	}

	private static void fillDefaults(Map<String, Double> results,
			Map<String, Double> defaults) {
		for (String param : defaults.keySet()) {
			Double r = results.get(param);
			if (r == null)
				results.put(param, defaults.get(param));
		}
	}

	private static Map<String, Double> getDefaultValues(
			List<Parameter> parameters) {
		Map<String, Double> defaultValues = new HashMap<>();
		for (Parameter parameter : parameters)
			defaultValues.put(parameter.getName(), parameter.getValue());
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

	private static Map<String, List<String>> groupParameters(
			List<Parameter> parameters) {
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
