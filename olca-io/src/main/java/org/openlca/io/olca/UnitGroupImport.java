package org.openlca.io.olca;

import java.util.Calendar;
import java.util.HashMap;

import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitGroupImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final UnitGroupDao srcDao;
	private final UnitGroupDao destDao;
	private final RefSwitcher refs;
	private final Seq seq;

	private final HashMap<String, UnitGroup> requirePropertyUpdate = new HashMap<>();

	UnitGroupImport(Config conf) {
		this.srcDao = new UnitGroupDao(conf.source());
		this.destDao = new UnitGroupDao(conf.target());
		this.refs = new RefSwitcher(conf);
		this.seq = conf.seq();
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
			for (var descriptor : srcDao.getDescriptors()) {
				if (seq.contains(Seq.UNIT_GROUP, descriptor.refId))
					synchUnitGroup(descriptor);
				else
					createUnitGroup(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import unit groups", e);
		}
	}

	private void synchUnitGroup(UnitGroupDescriptor descriptor) {
		UnitGroup srcGroup = srcDao.getForId(descriptor.id);
		UnitGroup destGroup = destDao.getForId(seq.get(Seq.UNIT_GROUP,
				descriptor.refId));
		boolean updated = false;
		for (Unit srcUnit : srcGroup.units) {
			Unit destUnit = destGroup.getUnit(srcUnit.name);
			if (!updated && destUnit != null) {
				seq.put(Seq.UNIT, srcUnit.refId, destUnit.id);
			} else {
				destUnit = srcUnit.copy();
				destUnit.refId = srcUnit.refId;
				destGroup.units.add(destUnit);
				updated = true;
			}
		}
		if (updated) {
			destGroup.lastChange = Calendar.getInstance().getTimeInMillis();
			Version.incUpdate(destGroup);
			destGroup = destDao.update(destGroup);
			indexUnits(srcGroup, destGroup);
			log.info("updated unit group {}", destGroup);
		}
	}

	private void createUnitGroup(UnitGroupDescriptor descriptor) {
		UnitGroup srcGroup = srcDao.getForId(descriptor.id);
		UnitGroup destGroup = srcGroup.copy();
		destGroup.refId = srcGroup.refId;
		switchUnitRefIds(srcGroup, destGroup);
		destGroup.defaultFlowProperty = null;
		destGroup.category = refs.switchRef(srcGroup.category);
		destGroup = destDao.insert(destGroup);
		seq.put(Seq.UNIT_GROUP, srcGroup.refId, destGroup.id);
		indexUnits(srcGroup, destGroup);
		FlowProperty defaultProperty = srcGroup.defaultFlowProperty;
		if (defaultProperty != null)
			requirePropertyUpdate.put(defaultProperty.refId, destGroup);
	}

	private void switchUnitRefIds(UnitGroup srcGroup, UnitGroup destGroup) {
		for (Unit srcUnit : srcGroup.units) {
			Unit destUnit = destGroup.getUnit(srcUnit.name);
			if (destUnit == null)
				continue;
			destUnit.refId = srcUnit.refId;
		}
	}

	private void indexUnits(UnitGroup srcGroup, UnitGroup destGroup) {
		for (Unit srcUnit : srcGroup.units) {
			Unit destUnit = destGroup.getUnit(srcUnit.name);
			if (destUnit == null) {
				log.error("failed to update unit group {}, {} is missing",
						destGroup, srcUnit);
				continue;
			}
			seq.put(Seq.UNIT, srcUnit.refId, destUnit.id);
		}
	}

}
