package org.openlca.io.hestia;

import org.openlca.core.database.IDatabase;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;

class UnitMap {

	private final UnitMapping units;

	private UnitMap(UnitMapping units) {
		this.units = units;
	}

	static UnitMap of(IDatabase db) {
		return new UnitMap(UnitMapping.createDefault(db));
	}

	UnitMappingEntry get(String symbol) {
		if (symbol == null)
			return null;
		var s = symbol.strip();
		var e = units.getEntry(s);
		if (e != null)
			return e;
		if (s.startsWith("kg "))
			return units.getEntry("kg");
		return null;
	}
}
