package org.openlca.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mappings of unit names to unit groups and flow properties in openLCA.
 */
public class UnitMapping {

	private HashMap<String, UnitMappingEntry> entries = new HashMap<>();

	/**
	 * Creates a default mapping for the unit names in the database.
	 */
	public static UnitMapping createDefault(IDatabase database) {
		Logger log = LoggerFactory.getLogger(UnitMapping.class);
		log.trace("create default mappings");
		UnitMapping mapping = new UnitMapping();
		try {
			for (UnitGroup group : new UnitGroupDao(database).getAll()) {
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
			List<String> names = getNames(unit);
			for (String name : names) {
				UnitMappingEntry entry = new UnitMappingEntry();
				entry.factor = unit.getConversionFactor();
				entry.flowProperty = prop;
				entry.unit = unit;
				entry.unitGroup = group;
				entry.unitName = name;
				mapping.put(name, entry);
			}
		}
	}

	private static FlowProperty findProperty(IDatabase database, UnitGroup group) {
		FlowPropertyDao dao = new FlowPropertyDao(database);
		for (FlowProperty prop : dao.getAll()) {
			if (Objects.equals(group, prop.getUnitGroup()))
				return prop;
		}
		return null;
	}

	/**
	 * Returns the name and the synonyms (so all unit symbols) for the given
	 * unit in a single list.
	 */
	public static List<String> getNames(Unit unit) {
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
		UnitMappingEntry entry = entries.get(unitName);
		return entry == null ? null : entry.factor;
	}

	public FlowProperty getFlowProperty(String unitName) {
		UnitMappingEntry entry = entries.get(unitName);
		return entry == null ? null : entry.flowProperty;
	}

	public UnitGroup getUnitGroup(String unitName) {
		UnitMappingEntry entry = entries.get(unitName);
		return entry == null ? null : entry.unitGroup;
	}

	public String[] getUnits() {
		return entries.keySet().toArray(new String[entries.size()]);
	}

	public void put(String unitName, UnitMappingEntry entry) {
		entries.put(unitName, entry);
	}

	/**
	 * Get the mapping entry for the given unit name or null if no such entry is
	 * contained in this mapping.
	 */
	public UnitMappingEntry getEntry(String unitName) {
		return entries.get(unitName);
	}

}
