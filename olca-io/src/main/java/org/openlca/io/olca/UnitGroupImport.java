package org.openlca.io.olca;

import java.util.Calendar;
import java.util.HashMap;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitGroupImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private UnitGroupDao srcDao;
	private UnitGroupDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	private HashMap<String, UnitGroup> requirePropertyUpdate = new HashMap<>();

	UnitGroupImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new UnitGroupDao(source);
		this.destDao = new UnitGroupDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	/**
	 * Returns a map with UUIDs of flow properties and corresponding unit groups
	 * that should have these flow properties assigned as default flow property.
	 * This can be done *after* the flow property import.
	 */
	public HashMap<String, UnitGroup> getRequirePropertyUpdate() {
		return requirePropertyUpdate;
	}

	public void run() {
		log.trace("import unit groups");
		try {
			for (UnitGroupDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.UNIT_GROUP, descriptor.getRefId()))
					synchUnitGroup(descriptor);
				else
					createUnitGroup(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import unit groups", e);
		}
	}

	private void synchUnitGroup(UnitGroupDescriptor descriptor) {
		UnitGroup srcGroup = srcDao.getForId(descriptor.getId());
		UnitGroup destGroup = destDao.getForId(seq.get(seq.UNIT_GROUP,
				descriptor.getRefId()));
		boolean updated = false;
		for (Unit srcUnit : srcGroup.getUnits()) {
			Unit destUnit = destGroup.getUnit(srcUnit.getName());
			if (!updated && destUnit != null) {
				seq.put(seq.UNIT, srcUnit.getRefId(), destUnit.getId());
			} else {
				destUnit = srcUnit.clone();
				destUnit.setRefId(srcUnit.getRefId());
				destGroup.getUnits().add(destUnit);
				updated = true;
			}
		}
		if (updated) {
			destGroup.setLastChange(Calendar.getInstance().getTimeInMillis());
			Version.incUpdate(destGroup);
			destGroup = destDao.update(destGroup);
			indexUnits(srcGroup, destGroup);
			log.info("updated unit group {}", destGroup);
		}
	}

	private void createUnitGroup(UnitGroupDescriptor descriptor) {
		UnitGroup srcGroup = srcDao.getForId(descriptor.getId());
		UnitGroup destGroup = srcGroup.clone();
		destGroup.setRefId(srcGroup.getRefId());
		switchUnitRefIds(srcGroup, destGroup);
		destGroup.setDefaultFlowProperty(null);
		destGroup.setCategory(refs.switchRef(srcGroup.getCategory()));
		destGroup = destDao.insert(destGroup);
		seq.put(seq.UNIT_GROUP, srcGroup.getRefId(), destGroup.getId());
		indexUnits(srcGroup, destGroup);
		FlowProperty defaultProperty = srcGroup.getDefaultFlowProperty();
		if (defaultProperty != null)
			requirePropertyUpdate.put(defaultProperty.getRefId(), destGroup);
	}

	private void switchUnitRefIds(UnitGroup srcGroup, UnitGroup destGroup) {
		for (Unit srcUnit : srcGroup.getUnits()) {
			Unit destUnit = destGroup.getUnit(srcUnit.getName());
			if (destUnit == null)
				continue;
			destUnit.setRefId(srcUnit.getRefId());
		}
	}

	private void indexUnits(UnitGroup srcGroup, UnitGroup destGroup) {
		for (Unit srcUnit : srcGroup.getUnits()) {
			Unit destUnit = destGroup.getUnit(srcUnit.getName());
			if (destUnit == null) {
				log.error("failed to update unit group {}, {} is missing",
						destGroup, srcUnit);
				continue;
			}
			seq.put(seq.UNIT, srcUnit.getRefId(), destUnit.getId());
		}
	}

}
