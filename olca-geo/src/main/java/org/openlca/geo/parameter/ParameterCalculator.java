package org.openlca.geo.parameter;

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

public class ParameterCalculator {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<String, List<String>> groups;
	private Map<String, DataStore> stores;
	private Map<String, Double> defaults;
	private ParameterRepository repository;

	public ParameterCalculator(List<Parameter> parameters,
			ShapeFileRepository shapeFileRepository,
			ParameterRepository parameterRepository) {
		groups = groupParameters(parameters);
		stores = openStores(groups.keySet(), shapeFileRepository);
		defaults = getDefaultValues(parameters);
		repository = parameterRepository;
	}

	public Map<String, Double> calculate(long locationId, KmlFeature feature) {
		Map<String, Double> parameterMap = new HashMap<String, Double>();
		for (String shapeFile : groups.keySet()) {
			Map<String, Double> shares = loadOrCalculateShares(locationId,
					feature, shapeFile);
			Map<String, Double> parameters = applyParameters(shares,
					locationId, feature, shapeFile);
			parameterMap.putAll(parameters);
		}
		fillDefaults(parameterMap, defaults);
		return parameterMap;
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

	private Map<String, Double> loadOrCalculateShares(long locationId,
			KmlFeature feature, String shapeFile) {
		Map<String, Double> result = repository.load(locationId, shapeFile);
		if (result != null)
			return result;
		DataStore store = stores.get(shapeFile);
		IntersectionsCalculator calculator = new IntersectionsCalculator(store);
		List<String> group = groups.get(shapeFile);
		log.debug("Calculating shares for location " + locationId);
		result = calculator.calculate(feature, group);
		repository.save(locationId, shapeFile, result);
		return result != null ? result : new HashMap<String, Double>();
	}

	private Map<String, Double> applyParameters(Map<String, Double> shares,
			long id, KmlFeature feature, String shapeFile) {
		DataStore store = stores.get(shapeFile);
		FeatureCalculator calculator = new FeatureCalculator(store);
		List<String> group = groups.get(shapeFile);
		log.debug("Calculating parameters for location " + id);
		Map<String, Double> result = calculator.calculate(feature, group,
				defaults, shares);
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
