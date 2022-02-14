package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProductType;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.refdata.QuantityRow;
import org.openlca.simapro.csv.refdata.UnitRow;
import org.openlca.util.KeyGen;

/**
 * Synchronizes the used unit names found in a SimaPro CSV file with the units
 * in a database. Normally all units should be already exist in the database.
 * Otherwise, the corresponding unit group, flow property, and unit entries are
 * created.
 */
class UnitSync {

	private final ImportLog log;
	private final IDatabase db;
	private final UnitMapping mapping;

	UnitSync(IDatabase db, ImportLog log) {
		this.db = db;
		this.log = log;
		this.mapping = UnitMapping.createDefault(db);
	}

	UnitMapping mapping() {
		return mapping;
	}

	/**
	 * Adds the units of the given data set to the unit mapping if they are
	 * missing.
	 */
	void sync(CsvDataSet dataSet) {
		if (dataSet == null)
			return;
		try {
			log.info("check units");
			var mapping = UnitMapping.createDefault(db);
			var unknownUnits = new ArrayList<String>();
			for (var unit : collectUnitsOf(dataSet)) {
				var entry = mapping.getEntry(unit);
				if (entry == null) {
					unknownUnits.add(unit);
				}
			}
			if (!unknownUnits.isEmpty()) {
				syncUnits(dataSet, unknownUnits);
			}
		} catch (Exception e) {
			log.error("failed to synchronize units with database", e);
		}
	}

	private void syncUnits(CsvDataSet dataSet, List<String> unknownUnits) {

		while (!unknownUnits.isEmpty()) {

			var unit = unknownUnits.remove(0);
			UnitRow unitRow = null;
			for (var row : dataSet.units()) {
				if (Objects.equals(unit, row.name())) {
					unitRow = row;
					break;
				}
			}

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
				log.warn("unit " + unit + " found but without quantity; create default "
					+ "unit, unit group, and flow property");
				createStandalone(unit, mapping);
				continue;
			}

			log.warn("unknown unit " + unit + "; import quantity " + quantityRow.name());
			var group = createForQuantity(dataSet, quantityRow, mapping);
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
		log.updated(group);

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
				log.error("Could not find " + u + " in " + group.name);
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


	private UnitGroup createForQuantity(
		CsvDataSet dataSet, QuantityRow quantity, UnitMapping mapping) {

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
		log.imported(group.defaultFlowProperty);
		group = db.update(group);
		log.imported(group);

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
	private void createStandalone(String unit, UnitMapping mapping) {
		var group = UnitGroup.of("Unit group for " + unit, unit);
		db.insert(group);
		var property = FlowProperty.of("Property for " + unit, group);
		group.defaultFlowProperty = db.insert(property);
		log.imported(group.defaultFlowProperty);
		group = db.update(group);
		log.imported(group);
		var e = new UnitMappingEntry();
		e.unitGroup = group;
		e.unit = group.referenceUnit;
		e.factor = 1d;
		e.flowProperty = group.defaultFlowProperty;
		e.unitName = unit;
		mapping.put(unit, e);
	}

	/**
	 * Collects the used units from the given data set.
	 */
	private Set<String> collectUnitsOf(CsvDataSet csv) {
		if (csv == null)
			return Collections.emptySet();
		var units = new HashSet<String>();

		// from flows
		for (var type : ElementaryFlowType.values()) {
			for (var f : csv.getElementaryFlows(type)) {
				units.add(f.unit());
			}
		}

		// from exchanges
		Consumer<List<? extends ExchangeRow>> exchanges = list -> {
			for (var e : list) {
				units.add(e.unit());
			}
		};
		for (var p: csv.processes()) {
			exchanges.accept(p.products());
			for (var type : ProductType.values()) {
				exchanges.accept(p.exchangesOf(type));
			}
			for (var type : ElementaryFlowType.values()) {
				exchanges.accept(p.exchangesOf(type));
			}
			if (p.wasteTreatment() != null) {
				exchanges.accept(List.of(p.wasteTreatment()));
			}
		}

		for (var s : csv.productStages()) {
			exchanges.accept(s.products());
			exchanges.accept(s.processes());
			exchanges.accept(s.additionalLifeCycles());
			exchanges.accept(s.disassemblies());
			exchanges.accept(s.materialsAndAssemblies());
			exchanges.accept(s.reuses());
			if (s.assembly() != null) {
				exchanges.accept(List.of(s.assembly()));
			}
			if (s.referenceAssembly() != null) {
				exchanges.accept(List.of(s.referenceAssembly()));
			}
		}

		// from LCIA factors
		for (var m : csv.methods()) {
			for (var i : m.impactCategories()) {
				for (var f : i.factors()) {
					units.add(f.unit());
				}
			}
		}

		return units;
	}
}
