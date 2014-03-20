package org.openlca.io.simapro.csv.input;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.model.refdata.QuantityRow;
import org.openlca.simapro.csv.model.refdata.UnitRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
				addMappings(mapping, unknownUnits);
			refData.setUnitMapping(mapping);
		} catch (Exception e) {
			log.error("failed to synchronize units with database", e);
			refData.setUnitMapping(new UnitMapping());
		}
	}

	private void addMappings(UnitMapping mapping, List<String> unknownUnits) {
		while (!unknownUnits.isEmpty()) {
			String unit = unknownUnits.remove(0);
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

	private UnitGroup importQuantity(QuantityRow quantity, UnitMapping mapping) {
		UnitGroup group = create(UnitGroup.class,
				"Units of " + quantity.getName());
		addUnits(group, quantity);
		group = insertLinkProperty(group, quantity.getName());
		for (Unit unit : group.getUnits()) {
			UnitMappingEntry entry = new UnitMappingEntry();
			entry.setFlowProperty(group.getDefaultFlowProperty());
			entry.setUnitName(unit.getName());
			entry.setUnit(unit);
			entry.setFactor(unit.getConversionFactor());
			entry.setUnitGroup(group);
			mapping.put(unit.getName(), entry);
		}
		return group;
	}

	private UnitGroup insertLinkProperty(UnitGroup group, String propertyName) {
		UnitGroupDao groupDao = new UnitGroupDao(database);
		group = groupDao.insert(group);
		FlowProperty property = create(FlowProperty.class, propertyName);
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
		UnitMappingEntry entry = createDefaultEntry(unitName, group);
		mapping.put(unitName, entry);
	}

	private UnitMappingEntry createDefaultEntry(String unitName, UnitGroup group) {
		UnitMappingEntry entry = new UnitMappingEntry();
		entry.setUnitGroup(group);
		entry.setUnit(group.getReferenceUnit());
		entry.setFactor(1d);
		entry.setFlowProperty(group.getDefaultFlowProperty());
		entry.setUnitName(unitName);
		return entry;
	}

	private <T extends RootEntity> T create(Class<T> clazz, String name) {
		try {
			T t = clazz.newInstance();
			t.setName(name);
			t.setRefId(UUID.randomUUID().toString());
			return t;
		} catch (Exception e) {
			log.error("");
		}
		return null;
	}

	private QuantityRow getQuantity(String unitName) {
		UnitRow row = index.getUnitRow(unitName);
		if (row == null)
			return null;
		return index.getQuantity(row.getQuantity());
	}
}
