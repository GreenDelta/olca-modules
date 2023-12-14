package org.openlca.io.oneclick;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import java.util.HashSet;
import java.util.Set;

class UnitMatcher {

	private final Set<Unit> transportUnits;
	private final Set<Unit> energyUnits;

	private UnitMatcher(IDatabase db) {
		transportUnits = new HashSet<>();
		energyUnits = new HashSet<>();
		for (var group : db.getAll(UnitGroup.class)) {
			if (group.name == null)
				continue;
			if (group.name.endsWith("*length")) {
				transportUnits.addAll(group.units);
				continue;
			}
			if ("Units of energy".equalsIgnoreCase(group.name)) {
				energyUnits.addAll(group.units);
			}
		}
	}

	static UnitMatcher create(IDatabase db) {
		return new UnitMatcher(db);
	}

	boolean isEnergyUnit(Unit unit) {
		return energyUnits.contains(unit);
	}

	boolean isTransportUnit(Unit unit) {
		return transportUnits.contains(unit);
	}
}
