package org.openlca.io.ecospold2.output;

import java.util.Objects;

import org.openlca.core.model.Unit;

import spold2.Exchange;
import spold2.UserMasterData;

final class Units {

	private Units() {
	}

	static void map(Unit unit, Exchange exchange, UserMasterData masterData) {
		if (unit == null || exchange == null)
			return;
		exchange.unit = unit.name;
		exchange.unitId = unit.refId;
		if (masterData == null)
			return;
		for (spold2.Unit es2Unit : masterData.units) {
			if (Objects.equals(unit.refId, es2Unit.id))
				return;
		}
		spold2.Unit es2Unit = new spold2.Unit();
		es2Unit.comment = unit.description;
		es2Unit.id = unit.refId;
		es2Unit.name = unit.name;
		masterData.units.add(es2Unit);
	}
}
