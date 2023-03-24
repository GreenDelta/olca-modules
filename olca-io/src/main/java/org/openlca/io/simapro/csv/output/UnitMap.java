package org.openlca.io.simapro.csv.output;

import java.util.Collection;
import java.util.Collections;
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
			return SimaProUnit.kg.symbol;
		SimaProUnit unit = units.get(u.name);
		if (unit != null)
			return unit.symbol;
		unit = SimaProUnit.find(u.name);
		if (unit != null) {
			units.put(u.name, unit);
			return unit.symbol;
		}
		if (u.synonyms != null) {
			for (String syn : u.synonyms.split(";")) {
				unit = SimaProUnit.find(syn);
				if (unit != null) {
					units.put(u.name, unit);
					return unit.symbol;
				}
			}
		}
		LoggerFactory.getLogger(getClass())
				.warn("No corresponding SimaPro unit" +
						" for '{}' found; fall back to 'kg'", u.name);
		units.put(u.name, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	/**
	 * Returns the corresponding SimaPro name of the given unit name.
	 */
	String get(String u) {
		if (u == null)
			return SimaProUnit.kg.symbol;
		SimaProUnit unit = units.get(u);
		if (unit != null)
			return unit.symbol;
		unit = SimaProUnit.find(u);
		if (unit != null) {
			units.put(u, unit);
			return unit.symbol;
		}
		LoggerFactory.getLogger(getClass())
				.warn("No corresponding SimaPro unit" +
						" for '{}' found; fall back to 'kg'", u);
		units.put(u, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	Collection<SimaProUnit> values() {
		return units.values();
	}
}
