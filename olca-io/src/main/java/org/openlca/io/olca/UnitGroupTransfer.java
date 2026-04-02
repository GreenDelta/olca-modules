package org.openlca.io.olca;

import org.openlca.core.model.UnitGroup;

public final class UnitGroupTransfer implements EntityTransfer<UnitGroup>{

	private final TransferConfig conf;

	public UnitGroupTransfer(TransferConfig conf) {
		this.conf = conf;
	}

	@Override
	public void syncAll() {
		for (var group : conf.source().getAll(UnitGroup.class)) {
			sync(group);
		}
	}

	@Override
	public UnitGroup sync(UnitGroup origin) {
		if (origin == null) return null;
		var mapped = conf.getMapped(origin);
		if (mapped != null) return mapped;

		var copy = origin.copy();
		copy.refId = origin.refId;
		copy.category = conf.swap(origin.category);
		copy.defaultFlowProperty = null; // break possible cycles
		switchUnitRefIds(origin, copy);

		copy = conf.save(origin.id, copy);
		if (origin.defaultFlowProperty != null) {
			copy.defaultFlowProperty = conf.swap(origin.defaultFlowProperty);
			copy = conf.save(origin.id, copy);
		}
		return copy;
	}

	private void switchUnitRefIds(UnitGroup group, UnitGroup copy) {
		for (var unit : group.units) {
			var unitCopy = copy.getUnit(unit.name);
			if (unitCopy == null) continue;
			unitCopy.refId = unit.refId;
		}
	}
}
