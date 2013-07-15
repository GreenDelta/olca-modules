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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mappings of unit names to unit groups and flow properties in openLCA.
 */
public class UnitMapping {

	private HashMap<String, Double> factors = new HashMap<>();
	private HashMap<String, FlowProperty> flowPropertyMappings = new HashMap<>();
	private HashMap<String, UnitGroup> unitGroupMappings = new HashMap<>();
	private HashMap<String, UnitMappingEntry> cachedEntries = new HashMap<>();

	/**
	 * Creates a default mapping for the unit names in the database.
	 */
	public static UnitMapping createDefault(IDatabase database) {
		Logger log = LoggerFactory.getLogger(UnitMapping.class);
		log.trace("create default mappings");
		UnitMapping mapping = new UnitMapping();
		try {
			for (UnitGroup group : database.createDao(UnitGroup.class).getAll()) {
				FlowProperty prop = group.getDefaultFlowProperty();
				if (prop == null)
					prop = findProperty(database, group);
				if (prop == null) {
					log.warn("no flow property found for unit group {}", group);
					continue;
				}
				registerUnits(group, prop, mapping);
			}
		} catch (Exception e) {
			log.error("failed to init. unit mapping", e);
		}
		return mapping;
	}

	private static void registerUnits(UnitGroup group, FlowProperty prop,
			UnitMapping mapping) {
		for (Unit unit : group.getUnits()) {
			List<String> names = unitNames(unit);
			for (String name : names) {
				mapping.put(name, prop, group,
						unit.getConversionFactor());
			}
		}
	}

	private static FlowProperty findProperty(IDatabase database, UnitGroup group)
			throws Exception {
		BaseDao<FlowProperty> dao = database.createDao(FlowProperty.class);
		for (FlowProperty prop : dao.getAll()) {
			if (Objects.equals(group, prop.getUnitGroup()))
				return prop;
		}
		return null;
	}

	private static List<String> unitNames(Unit unit) {
		if (unit == null)
			return Collections.emptyList();
		List<String> names = new ArrayList<>();
		if (unit.getName() != null)
			names.add(unit.getName());
		if (unit.getSynonyms() == null || unit.getSynonyms().isEmpty())
			return names;
		for (String synonym : unit.getSynonyms().split(";"))
			names.add(synonym.trim());
		return names;
	}

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
