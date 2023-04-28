package org.openlca.io.simapro.csv.output;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.model.Unit;
import org.openlca.io.simapro.csv.SimaProUnit;
import org.slf4j.LoggerFactory;

class UnitMap {

	private final Map<String, SimaProUnit> units = new HashMap<>();

	/**
	 * Returns the corresponding SimaPro name of the given unit.
	 */
	String get(Unit u) {
		if (u == null)
			return null;

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

		// log error
		LoggerFactory.getLogger(getClass()).error(
				"No corresponding SimaPro unit found for '{}'", u.name);
		return null;
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

		// log error
		LoggerFactory.getLogger(getClass()).error(
				"No corresponding SimaPro unit found for '{}'", u);
		return null;
	}

	Collection<SimaProUnit> values() {
		return units.values();
	}
}
