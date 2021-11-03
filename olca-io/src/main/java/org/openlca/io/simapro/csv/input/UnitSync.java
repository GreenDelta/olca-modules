package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.refdata.QuantityRow;
import org.openlca.simapro.csv.refdata.UnitRow;
import org.openlca.util.KeyGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes the used unit names found in a SimaPro CSV file with the units
 * in a database. Normally all units should be already exist in the database.
 * Otherwise, the corresponding unit group, flow property, and unit entries are
 * created.
 */
class UnitSync {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final CsvDataSet dataSet;
	private final IDatabase db;

	public UnitSync(CsvDataSet dataSet, IDatabase database) {
		this.dataSet = dataSet;
		this.db = database;
	}

	public void run(RefData refData) {
		log.trace("synchronize units with database");
		try {
			var mapping = UnitMapping.createDefault(db);
			var unknownUnits = new ArrayList<String>();
			for (var unit : CsvUtil.allUnitsOf(dataSet)) {
				var entry = mapping.getEntry(unit);
				if (entry == null) {
					unknownUnits.add(unit);
				} else {
					log.trace("{} is a known unit", unit);
				}
			}
			if (!unknownUnits.isEmpty()) {
				syncUnits(mapping, unknownUnits);
			}
			refData.setUnitMapping(mapping);
		} catch (Exception e) {
			log.error("failed to synchronize units with database", e);
			refData.setUnitMapping(new UnitMapping());
		}
	}

	private void syncUnits(UnitMapping mapping, List<String> unknownUnits) {
		while (!unknownUnits.isEmpty()) {
			var unit = unknownUnits.remove(0);
			var row = CsvUtil.unitRowOf(dataSet, unit);
			if (row != null && mapping.hasEntry(row.referenceUnit())) {
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
				for (Unit u : group.units)
					unknownUnits.remove(u.name);
			}
		}
	}

	/** Add a new unit to an existing unit group. */
	private void addUnit(UnitRow row, UnitMapping mapping) {
		String name = row.name();
		var refEntry = mapping.getEntry(row.referenceUnit());
		double factor = row.conversionFactor() * refEntry.unit.conversionFactor;

		var unit = new Unit();
		unit.conversionFactor = factor;
		unit.name = name;
		unit.refId = KeyGen.get(name);

		var group = refEntry.unitGroup;
		group.units.add(unit);
		UnitGroupDao groupDao = new UnitGroupDao(db);
		group.lastChange = System.currentTimeMillis();
		Version.incUpdate(group);
		group = groupDao.update(group);

		log.info("added new unit {} to group {}", unit, group);

		FlowPropertyDao propDao = new FlowPropertyDao(db);
		FlowProperty property = propDao
				.getForId(refEntry.flowProperty.id);
		updateRefs(mapping, group, property);

		var newEntry = new UnitMappingEntry();
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
			Unit u = group.getUnit(entry.unit.name);
			if (u == null) {
				log.error("Could not find {} in {}", name, group);
				continue;
			}
			entry.flowProperty = property;
			entry.unitGroup = group;
			entry.unit = u;
		}
	}

	private UnitGroup importQuantity(QuantityRow quantity,
			UnitMapping mapping) {
		UnitGroup group = UnitGroup.of("Units of " + quantity.name);
		addUnits(group, quantity);
		group = insertLinkProperty(group, quantity.name);
		for (Unit unit : group.units) {
			UnitMappingEntry entry = new UnitMappingEntry();
			entry.flowProperty = group.defaultFlowProperty;
			entry.unitName = unit.name;
			entry.unit = unit;
			entry.factor = unit.conversionFactor;
			entry.unitGroup = group;
			mapping.put(unit.name, entry);
		}
		return group;
	}

	private UnitGroup insertLinkProperty(UnitGroup group, String name) {
		var dao = new UnitGroupDao(db);
		group = dao.insert(group);
		var property = FlowProperty.of(name, group);
		group.defaultFlowProperty = db.insert(property);
		return dao.update(group);
	}

	private void addUnits(UnitGroup group, QuantityRow quantity) {
		for (var row : index.getUnitRows()) {
			if (!Objects.equals(row.quantity, quantity.name))
				continue;
			var unit = Unit.of(row.name);
			unit.conversionFactor = row.conversionFactor;
			group.units.add(unit);
			if (Objects.equals(row.name, row.referenceUnit))
				group.referenceUnit = unit;
		}
	}

	private void createDefaultMapping(String unitName, UnitMapping mapping) {
		UnitGroup group = UnitGroup.of("Unit group for " + unitName, unitName);
		group = insertLinkProperty(group, "Property for " + unitName);
		UnitMappingEntry e = new UnitMappingEntry();
		e.unitGroup = group;
		e.unit = group.referenceUnit;
		e.factor = 1d;
		e.flowProperty = group.defaultFlowProperty;
		e.unitName = unitName;
		mapping.put(unitName, e);
	}

	private QuantityRow getQuantity(String unitName) {
		UnitRow row = index.getUnitRow(unitName);
		if (row == null)
			return null;
		return index.getQuantity(row.quantity);
	}
}
