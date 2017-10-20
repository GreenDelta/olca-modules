package org.openlca.io;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
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
			UnitGroup unitGroup = entry.unitGroup;
			String unitName = entry.unitName;
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
		log.trace("add new unit {} to group {}", entry.unitName, unitGroup);
		Unit unit = new Unit();
		unit.setName(entry.unitName);
		unit.setRefId(UUID.randomUUID().toString());
		double factor = entry.factor == null ? 1d : entry.factor;
		unit.setConversionFactor(factor);
		unitGroup.getUnits().add(unit);
		unitGroup.setLastChange(Calendar.getInstance().getTimeInMillis());
		Version.incUpdate(unitGroup);
		unitGroup = new UnitGroupDao(database).update(unitGroup);
		entry.factor = factor;
		entry.unitGroup = unitGroup;
		entry.unit = unitGroup.getUnit(entry.unitName);
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
			if (!Objects.equals(updatedGroup, entry.unitGroup))
				continue;
			Unit unit = updatedGroup.getUnit(entry.unitName);
			entry.unit = unit;
			entry.unitGroup = updatedGroup;
		}
	}

}
