package org.openlca.io.simapro.csv.input;

import java.util.HashMap;

import org.openlca.core.model.Flow;
import org.openlca.core.model.Source;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.MapFactor;

class RefData {

	private UnitMapping unitMapping;
	private HashMap<String, Flow> products = new HashMap<>();
	private HashMap<String, Flow> elemFlows = new HashMap<>();
	private HashMap<String, MapFactor<Flow>> mappedFlows = new HashMap<>();
	private HashMap<String, Source> sources = new HashMap<>();

	public void setUnitMapping(UnitMapping unitMapping) {
		this.unitMapping = unitMapping;
	}

	public UnitMapping getUnitMapping() {
		return unitMapping;
	}

	public UnitMappingEntry getUnitEntry(String unitName) {
		if (unitMapping == null)
			return null;
		else
			return unitMapping.getEntry(unitName);
	}

	public void putProduct(String key, Flow flow) {
		products.put(key, flow);
	}

	public Flow getProduct(String key) {
		return products.get(key);
	}

	public void putElemFlow(String key, Flow flow) {
		elemFlows.put(key, flow);
	}

	public Flow getElemFlow(String key) {
		return elemFlows.get(key);
	}

	public void putMappedFlow(String key, MapFactor<Flow> factor) {
		mappedFlows.put(key, factor);
	}

	public MapFactor<Flow> getMappedFlow(String key) {
		return mappedFlows.get(key);
	}

	public void putSource(String key, Source source) {
		sources.put(key, source);
	}

	public Source getSource(String key) {
		return sources.get(key);
	}

}
