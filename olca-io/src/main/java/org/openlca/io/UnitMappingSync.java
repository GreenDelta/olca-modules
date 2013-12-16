package org.openlca.io;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes a set of unit-mappings with the database. New units are created
 * in the respective unit groups.
 */
public class UnitMappingSync {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;

	public UnitMappingSync(IDatabase database) {
		this.database = database;
	}

	/**
	 * Runs the synchronization. The given entries are directly modified in the
	 * synchronization and added to the unit mapping.
	 */
	public UnitMapping run(List<UnitMappingEntry> entries) {
		UnitMapping mapping = new UnitMapping();
		for (UnitMappingEntry entry : entries) {
			UnitGroup unitGroup = entry.getUnitGroup();
			String unitName = entry.getUnitName();
			if (unitGroup.getUnit(unitName) != null) {
				mapping.put(unitName, entry);
				continue;
			}
			unitGroup = updateUnitGroup(entry, unitGroup);
			syncEntries(unitGroup, entries);
			mapping.put(unitName, entry);
		}
		return mapping;
	}

	/**
	 * Add a new unit created from the given entry to the unit group.
	 */
	private UnitGroup updateUnitGroup(UnitMappingEntry entry,
			UnitGroup unitGroup) {
		log.trace("add new unit {} to group {}", entry.getUnitName(), unitGroup);
		Unit unit = new Unit();
		unit.setName(entry.getUnitName());
		unit.setRefId(UUID.randomUUID().toString());
		double factor = entry.getFactor() == null ? 1d : entry.getFactor();
		unit.setConversionFactor(factor);
		unitGroup.getUnits().add(unit);
		unitGroup = database.createDao(UnitGroup.class).update(unitGroup);
		entry.setFactor(factor);
		entry.setUnitGroup(unitGroup);
		entry.setUnit(unitGroup.getUnit(entry.getUnitName()));
		return unitGroup;
	}

	/**
	 * Replace the unit group and units in the entries with the last updated
	 * versions. This ensures that all units and unit groups are synchronous
	 * with the persistence layer.
	 */
	private void syncEntries(UnitGroup updatedGroup,
			List<UnitMappingEntry> entries) {
		for (UnitMappingEntry entry : entries) {
			if (!Objects.equals(updatedGroup, entry.getUnitGroup()))
				continue;
			Unit unit = updatedGroup.getUnit(entry.getUnitName());
			entry.setUnit(unit);
			entry.setUnitGroup(updatedGroup);
		}
	}

}
