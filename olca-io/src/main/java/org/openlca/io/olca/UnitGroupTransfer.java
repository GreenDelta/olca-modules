package org.openlca.io.olca;

import org.openlca.core.model.UnitGroup;

final class UnitGroupTransfer implements EntityTransfer<UnitGroup>{

	private final TransferContext ctx;

	UnitGroupTransfer(TransferContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void syncAll() {
		for (var group : ctx.source().getAll(UnitGroup.class)) {
			sync(group);
		}
	}

	@Override
	public UnitGroup sync(UnitGroup origin) {
		if (origin == null) return null;
		var mapped = ctx.getMapped(origin);
		if (mapped != null) return mapped;

		var copy = origin.copy();
		copy.refId = origin.refId;
		copy.category = ctx.resolve(origin.category);
		copy.defaultFlowProperty = null; // break possible cycles
		switchUnitRefIds(origin, copy);

		copy = ctx.save(origin.id, copy);
		if (origin.defaultFlowProperty != null) {
			copy.defaultFlowProperty = ctx.resolve(origin.defaultFlowProperty);
			copy = ctx.save(origin.id, copy);
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
