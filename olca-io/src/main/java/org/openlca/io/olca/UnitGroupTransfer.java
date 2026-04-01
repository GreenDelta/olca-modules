package org.openlca.io.olca;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

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
	public UnitGroup sync(UnitGroup group) {
		if (group == null) return null;
		var mapped = conf.getMapped(group);
		if (mapped != null) return mapped;

		var copy = group.copy();
		copy.refId = group.refId;
		copy.category = conf.swap(group.category);
		copy.defaultFlowProperty = null; // break possible cycles
		switchUnitRefIds(group, copy);

		copy = sync()

	}

	private void copy(UnitGroupDescriptor d) {
		UnitGroup dest = src.copy();



		dest.category = conf.swap(src.category);
		dest = destDao.insert(dest);
		seq.put(ModelType.UNIT_GROUP, src.id, dest.id);
		if (src.defaultFlowProperty != null) {
			defaultLinks.add(new DefaultLink(dest, src.defaultFlowProperty.id));
		}
	}

	private void switchUnitRefIds(UnitGroup group, UnitGroup copy) {
		for (var unit : group.units) {
			var unitCopy = copy.getUnit(unit.name);
			if (unitCopy == null) continue;
			unitCopy.refId = unit.refId;
		}
	}

	/// Stores the default flow property link for a unit group. We need to set
	/// the link after the import of flow properties.
	record DefaultLink(UnitGroup targetUnitGroup, long sourcePropertyId) {
	}

}
