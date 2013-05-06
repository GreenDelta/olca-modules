/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.io;

import java.util.HashMap;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

/**
 * Mappings of unit names to unit groups and flow properties in openLCA.
 */
public class UnitMapping {

	private HashMap<String, Double> factors = new HashMap<>();
	private HashMap<String, FlowProperty> flowPropertyMappings = new HashMap<>();
	private HashMap<String, UnitGroup> unitGroupMappings = new HashMap<>();
	private HashMap<String, UnitMappingEntry> cachedEntries = new HashMap<>();

	public Double getConversionFactor(String unitName) {
		return factors.get(unitName);
	}

	public FlowProperty getFlowProperty(String unitName) {
		return flowPropertyMappings.get(unitName);
	}

	private UnitGroup getUnitGroup(String unitName) {
		return unitGroupMappings.get(unitName);
	}

	public String[] getUnits() {
		return factors.keySet().toArray(new String[factors.size()]);
	}

	public void put(String unitName, FlowProperty flowProperty,
			UnitGroup unitGroup, Double conversionFactor) {
		flowPropertyMappings.put(unitName, flowProperty);
		unitGroupMappings.put(unitName, unitGroup);
		factors.put(unitName, conversionFactor);
		cachedEntries.remove(unitName);
	}

	public void set(String unitName, Double conversionFactor) {
		factors.put(unitName, conversionFactor);
		cachedEntries.remove(unitName);
	}

	public void set(String unitName, FlowProperty flowProperty,
			UnitGroup unitGroup) {
		flowPropertyMappings.put(unitName, flowProperty);
		unitGroupMappings.put(unitName, unitGroup);
		cachedEntries.remove(unitName);
	}

	public UnitMappingEntry getEntry(String unitName) {
		UnitMappingEntry entry = cachedEntries.get(unitName);
		if (entry != null)
			return entry;
		entry = new UnitMappingEntry();
		Double factor = getConversionFactor(unitName);
		entry.setFactor(factor == null ? 1.0 : factor);
		entry.setFlowProperty(getFlowProperty(unitName));
		entry.setUnit(getUnit(unitName));
		entry.setUnitGroup(getUnitGroup(unitName));
		entry.setUnitName(unitName);
		if (entry.isValid())
			cachedEntries.put(unitName, entry);
		return entry;
	}

	private Unit getUnit(String unitName) {
		if (unitName == null)
			return null;
		UnitGroup unitGroup = getUnitGroup(unitName);
		if (unitGroup == null)
			return null;
		return unitGroup.getUnit(unitName);
	}

}
