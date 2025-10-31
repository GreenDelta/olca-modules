package org.openlca.io.simapro.csv.output;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.Unit;
import org.openlca.io.simapro.csv.SimaProUnit;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.refdata.QuantityRow;
import org.openlca.simapro.csv.refdata.UnitRow;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

class UnitMap {

	private final Map<String, SimaProUnit> units = new HashMap<>();
	private final Set<String> unmapped = new HashSet<String>();

	/**
	 * Returns the corresponding SimaPro name of the given unit.
	 */
	String get(Unit u) {
		if (u == null)
			return "?";

		// get mapped
		var unit = units.get(u.name);
		if (unit != null)
			return unit.symbol;

		// find by name
		unit = SimaProUnit.find(u.name);
		if (unit != null) {
			units.put(u.name, unit);
			return unit.symbol;
		}

		// find by synonym
		if (u.synonyms != null) {
			for (String syn : u.synonyms.split(";")) {
				unit = SimaProUnit.find(syn);
				if (unit != null) {
					units.put(u.name, unit);
					return unit.symbol;
				}
			}
		}

		return unmappedOf(u.name);
	}

	/**
	 * Returns the corresponding SimaPro name of the given unit name.
	 */
	String get(String u) {
		if (u == null)
			return null;

		// get mapped
		var unit = units.get(u);
		if (unit != null)
			return unit.symbol;

		// find by name
		unit = SimaProUnit.find(u);
		if (unit != null) {
			units.put(u, unit);
			return unit.symbol;
		}

		return unmappedOf(u);
	}

	private String unmappedOf(String unit) {
		if (Strings.isBlank(unit))
			return "?";
		if (unmapped.contains(unit))
			return unit;
		LoggerFactory.getLogger(getClass()).warn(
				"No corresponding SimaPro unit found for '{}'", unit);
		unmapped.add(unit);
		return unit;
	}

	Collection<SimaProUnit> values() {
		return units.values();
	}

	/**
	 * Write the quantities and their units of the used
	 * units to the given data set.
	 */
	void writeQuantitiesTo(CsvDataSet ds) {
		if (ds == null)
			return;

		units.values().stream()
				.map(u -> u.quantity)
				.distinct()
				.map(q -> new QuantityRow().name(q).hasDimension(true))
				.forEach(ds.quantities()::add);

		units.values().stream()
				.map(u -> new UnitRow()
						.name(u.symbol)
						.quantity(u.quantity)
						.referenceUnit(u.refUnit)
						.conversionFactor(u.factor))
				.forEach(ds.units()::add);
	}
}
