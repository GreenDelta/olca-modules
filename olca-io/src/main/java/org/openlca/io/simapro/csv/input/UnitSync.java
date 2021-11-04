package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
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
			var unitRow = CsvUtil.unitRowOf(dataSet, unit);

			// add the unit to an existing unit group
			if (unitRow != null && mapping.hasEntry(unitRow.referenceUnit())) {
				addUnit(unitRow, mapping);
				continue;
			}

			// find the quantity row
			QuantityRow quantityRow = null;
			if (unitRow != null) {
				for (var q : dataSet.quantities()) {
					if (Objects.equals(unitRow.quantity(), q.name())) {
						quantityRow = q;
						break;
					}
				}
			}

			if (quantityRow == null) {
				log.warn("unit {} found but with no quantity; create default "
					+ "unit, unit group, and flow property", unit);
				createAllForUnit(unit, mapping);
				continue;
			}

			log.warn("unknown unit {}, import quantity {}", unit, quantityRow);
			var group = createForQuantity(quantityRow, mapping);
			for (var u : group.units) {
				unknownUnits.remove(u.name);
			}
		}
	}

	/**
	 * Add a new unit to an existing unit group.
	 */
	private void addUnit(UnitRow row, UnitMapping mapping) {
		String name = row.name();
		var refEntry = mapping.getEntry(row.referenceUnit());
		var group = refEntry.unitGroup;

		// create and add the new unit; note that the reference
		// unit can be different from the reference unit in SimaPro;
		// thus, we need to multiply the conversion factors here
		var newUnit = Unit.of(name,
			row.conversionFactor() * refEntry.unit.conversionFactor);
		newUnit.refId = KeyGen.get(group.refId, name);
		group.units.add(newUnit);

		// update the unit group
		group.lastChange = System.currentTimeMillis();
		Version.incUpdate(group);
		group = db.update(group);
		log.info("added new unit {} to group {}", newUnit, group);

		// reload object references in the mapping entries so that
		// we can use them directly in the JPA persistence
		var property = db.get(FlowProperty.class, refEntry.flowProperty.id);
		for (var u : mapping.getUnits()) {
			var entry = mapping.getEntry(u);
			if (!entry.isValid())
				continue;
			if (!Objects.equals(group, entry.unitGroup))
				continue;
			var unit = group.getUnit(entry.unit.name);
			if (unit == null) {
				log.error("Could not find {} in {}", u, group);
				continue;
			}
			entry.unitGroup = group;
			entry.unit = unit;
			if (Objects.equals(property, entry.flowProperty)) {
				entry.flowProperty = property;
			}
		}

		// add a new mapping entry
		var newEntry = new UnitMappingEntry();
		newEntry.factor = newUnit.conversionFactor;
		newEntry.flowProperty = property;
		newEntry.unit = group.getUnit(name); // reload it for JPA
		newEntry.unitGroup = group;
		newEntry.unitName = name;
		mapping.put(name, newEntry);
	}


	private UnitGroup createForQuantity(QuantityRow quantity, UnitMapping mapping) {

		// create unit group and flow property
		var group = UnitGroup.of("Units of " + quantity.name());
		for (var row : dataSet.units()) {
			if (!Objects.equals(row.quantity(), quantity.name()))
				continue;
			var unit = Unit.of(row.name(), row.conversionFactor());
			group.units.add(unit);
			if (Objects.equals(row.name(), row.referenceUnit())) {
				group.referenceUnit = unit;
			}
		}
		group = db.insert(group);
		group.defaultFlowProperty = db.insert(
			FlowProperty.of(quantity.name(), group));
		group = db.update(group);

		// create the mapping entries
		for (Unit unit : group.units) {
			var e = new UnitMappingEntry();
			e.flowProperty = group.defaultFlowProperty;
			e.unitName = unit.name;
			e.unit = unit;
			e.factor = unit.conversionFactor;
			e.unitGroup = group;
			mapping.put(unit.name, e);
		}
		return group;
	}


	/**
	 * Creates a new unit group and flow property for the given unit name and
	 * adds a mapping for this.
	 */
	private void createAllForUnit(String unit, UnitMapping mapping) {
		var group = UnitGroup.of("Unit group for " + unit, unit);
		db.insert(group);
		var property = FlowProperty.of("Property for " + unit, group);
		group.defaultFlowProperty = db.insert(property);
		group = db.update(group);
		var e = new UnitMappingEntry();
		e.unitGroup = group;
		e.unit = group.referenceUnit;
		e.factor = 1d;
		e.flowProperty = group.defaultFlowProperty;
		e.unitName = unit;
		mapping.put(unit, e);
	}

}
