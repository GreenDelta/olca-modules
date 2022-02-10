package org.openlca.io.ecospold2.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.Unit;

/**
 * An index with cached reference data for an EcoSpold 02 import.
 */
class RefDataIndex {

	private final Map<String, Long> processIds = new HashMap<>();
	private final Map<String, Category> processCategories = new HashMap<>();
	private final Map<String, Category> compartments = new HashMap<>();
	private final Map<String, Location> locations = new HashMap<>();
	private final Map<String, Unit> units = new HashMap<>();
	private final Map<String, FlowProperty> flowProperties = new HashMap<>();
	private final Map<String, Flow> flows = new HashMap<>();
	private final Map<String, Boolean> mappedFlow = new HashMap<>();
	private final Map<String, Double> flowFactor = new HashMap<>();

	public Category getProcessCategory(String key) {
		return processCategories.get(key);
	}

	public void putProcessCategory(String key, Category category) {
		processCategories.put(key, category);
	}

	public Location getLocation(String key) {
		return locations.get(key);
	}

	public void putLocation(String key, Location location) {
		locations.put(key, location);
	}

	public Unit getUnit(String key) {
		return units.get(key);
	}

	public void putUnit(String key, Unit unit) {
		units.put(key, unit);
	}

	/**
	 * Get the flow property for the given unit ID (note that EcoSpold 2 does
	 * not have the concept of flow properties).
	 */
	public FlowProperty getFlowProperty(String unitID) {
		return flowProperties.get(unitID);
	}

	/**
	 * Register the given flow property for the given unit ID (note that
	 * EcoSpold 2 does not have the concept of flow properties).
	 */
	public void putFlowProperty(String unitID, FlowProperty property) {
		flowProperties.put(unitID, property);
	}

	public Category getCompartment(String key) {
		return compartments.get(key);
	}

	public void putCompartment(String key, Category category) {
		compartments.put(key, category);
	}

	public Flow getFlow(String key) {
		return flows.get(key);
	}

	public void putFlow(String key, Flow flow) {
		flows.put(key, flow);
	}

	public void putProcessId(String key, long id) {
		processIds.put(key, id);
	}

	public Long getProcessId(String key) {
		return processIds.get(key);
	}

	/**
	 * Sets the (elementary) flow with the given ID as mapped flow with the
	 * given conversion factor.
	 */
	public void putMappedFlow(String key, double factor) {
		mappedFlow.put(key, Boolean.TRUE);
		flowFactor.put(key, factor);
	}

	/**
	 * Returns true if the flow with the given ID is mapped to an openLCA
	 * reference flow.
	 */
	public boolean isMappedFlow(String key) {
		Boolean b = mappedFlow.get(key);
		if (b == null)
			return false;
		return b;
	}

	/**
	 * Returns the conversion factor which should be apply to an exchange amount
	 * if the flow with the given key is a mapped flow.
	 */
	public double getMappedFlowFactor(String key) {
		Double factor = flowFactor.get(key);
		if (factor == null)
			return 1;
		return factor;
	}

}
