package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.model.refdata.QuantityRow;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes the used unit names found in a SimaPro CSV file with the units
 * in a database. Normally all units should be already exist in the database.
 * Otherwise the corresponding unit group, flow property and, unit entries are
 * created.
 */
class UnitSync {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final SpRefDataIndex index;
	private final IDatabase database;

	public UnitSync(SpRefDataIndex index, IDatabase database) {
		this.index = index;
		this.database = database;
	}

	public void run(RefData refData) {
		log.trace("synchronize units with database");
		try {
			UnitMapping mapping = UnitMapping.createDefault(database);
			List<String> unknownUnits = new ArrayList<>();
			for (String usedUnit : index.getUsedUnits()) {
				UnitMappingEntry entry = mapping.getEntry(usedUnit);
				if (entry == null)
					unknownUnits.add(usedUnit);
				else
					log.trace("{} is a known unit", usedUnit);
			}
			if (!unknownUnits.isEmpty())
				syncUnits(mapping, unknownUnits);
			refData.setUnitMapping(mapping);
		} catch (Exception e) {
			log.error("failed to synchronize units with database", e);
			refData.setUnitMapping(new UnitMapping());
		}
	}

	private void syncUnits(UnitMapping mapping, List<String> unknownUnits) {
		while (!unknownUnits.isEmpty()) {
			String unit = unknownUnits.remove(0);
			UnitRow row = index.getUnitRow(unit);
			if (row != null
					&& mapping.getEntry(row.getReferenceUnit()) != null) {
				addUnit(row, mapping);
				continue;
			}
			QuantityRow quantity = getQuantity(unit);
			if (quantity == null) {
				log.warn("unit {} found but with no quantity; create default "
						+ "unit, unit group, and flow property", unit);
				createDefaultMapping(unit, mapping);
			} else {
				log.warn(
						"unknown unit {}, import complete SimaPro quantity {}",
						unit, quantity);
				UnitGroup group = importQuantity(quantity, mapping);
				for (Unit u : group.getUnits())
					unknownUnits.remove(u.getName());
			}
		}
	}

	/** Add a new unit to an existing unit group. */
	private void addUnit(UnitRow row, UnitMapping mapping) {
		String name = row.getName();
		UnitMappingEntry refEntry = mapping.getEntry(row.getReferenceUnit());
		double factor = row.getConversionFactor()
				* refEntry.unit.getConversionFactor();
		Unit unit = new Unit();
		unit.setConversionFactor(factor);
		unit.setName(name);
		unit.setRefId(KeyGen.get(name));
		UnitGroup group = refEntry.unitGroup;
		group.getUnits().add(unit);
		UnitGroupDao groupDao = new UnitGroupDao(database);
		group.setLastChange(Calendar.getInstance().getTimeInMillis());
		Version.incUpdate(group);
		group = groupDao.update(group);
		log.info("added new unit {} to group {}", unit, group);
		FlowPropertyDao propDao = new FlowPropertyDao(database);
		FlowProperty property = propDao
				.getForId(refEntry.flowProperty.getId());
		updateRefs(mapping, group, property);
		UnitMappingEntry newEntry = new UnitMappingEntry();
		newEntry.factor = factor;
		newEntry.flowProperty = property;
		newEntry.unit = group.getUnit(name);
		newEntry.unitGroup = group;
		newEntry.unitName = name;
		mapping.put(name, newEntry);
	}

	private void updateRefs(UnitMapping mapping, UnitGroup group,
			FlowProperty property) {
		for (String name : mapping.getUnits()) {
			UnitMappingEntry entry = mapping.getEntry(name);
			if (!entry.isValid())
				continue;
			if (!Objects.equals(group, entry.unitGroup)
					|| !Objects.equals(property, entry.flowProperty))
				continue;
			Unit u = group.getUnit(entry.unit.getName());
			if (u == null) {
				log.error("Could not find {} in {}", u, group);
				continue;
			}
			entry.flowProperty = property;
			entry.unitGroup = group;
			entry.unit = u;
		}
	}

	private UnitGroup importQuantity(QuantityRow quantity,
			UnitMapping mapping) {
		UnitGroup group = create(UnitGroup.class,
				"Units of " + quantity.getName());
		addUnits(group, quantity);
		group = insertLinkProperty(group, quantity.getName());
		for (Unit unit : group.getUnits()) {
			UnitMappingEntry entry = new UnitMappingEntry();
			entry.flowProperty = group.getDefaultFlowProperty();
			entry.unitName = unit.getName();
			entry.unit = unit;
			entry.factor = unit.getConversionFactor();
			entry.unitGroup = group;
			mapping.put(unit.getName(), entry);
		}
		return group;
	}

	private UnitGroup insertLinkProperty(UnitGroup group, String propertyName) {
		UnitGroupDao groupDao = new UnitGroupDao(database);
		group = groupDao.insert(group);
		FlowProperty property = create(FlowProperty.class, propertyName);
		property.setFlowPropertyType(FlowPropertyType.PHYSICAL);
		property.setUnitGroup(group);
		FlowPropertyDao propertyDao = new FlowPropertyDao(database);
		property = propertyDao.insert(property);
		group.setDefaultFlowProperty(property);
		groupDao.update(group);
		return group;
	}

	private void addUnits(UnitGroup unitGroup, QuantityRow quantity) {
		for (UnitRow row : index.getUnitRows()) {
			if (!Objects.equals(row.getQuantity(), quantity.getName()))
				continue;
			Unit unit = create(Unit.class, row.getName());
			unit.setConversionFactor(row.getConversionFactor());
			unitGroup.getUnits().add(unit);
			if (Objects.equals(row.getName(), row.getReferenceUnit()))
				unitGroup.setReferenceUnit(unit);
		}
	}

	private void createDefaultMapping(String unitName, UnitMapping mapping) {
		Unit unit = create(Unit.class, unitName);
		unit.setConversionFactor(1);
		UnitGroup group = create(UnitGroup.class, "Unit group for " + unitName);
		group.getUnits().add(unit);
		group.setReferenceUnit(unit);
		group = insertLinkProperty(group, "Property for " + unitName);
		UnitMappingEntry e = new UnitMappingEntry();
		e.unitGroup = group;
		e.unit = group.getReferenceUnit();
		e.factor = 1d;
		e.flowProperty = group.getDefaultFlowProperty();
		e.unitName = unitName;
		mapping.put(unitName, e);
	}

	private <T extends RootEntity> T create(Class<T> clazz, String name) {
		try {
			T t = clazz.newInstance();
			t.setName(name);
			t.setRefId(UUID.randomUUID().toString());
			return t;
		} catch (Exception e) {
			log.error("failed to create " + clazz, e);
			return null;
		}
	}

	private QuantityRow getQuantity(String unitName) {
		UnitRow row = index.getUnitRow(unitName);
		if (row == null)
			return null;
		return index.getQuantity(row.getQuantity());
	}
}
