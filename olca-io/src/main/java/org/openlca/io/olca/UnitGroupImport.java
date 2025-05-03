package org.openlca.io.olca;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UnitGroupImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final UnitGroupDao srcDao;
	private final UnitGroupDao destDao;
	private final RefSwitcher refs;
	private final SeqMap seq;

	private final List<DefaultLink> defaultLinks = new ArrayList<>();

	UnitGroupImport(Config conf) {
		this.srcDao = new UnitGroupDao(conf.source());
		this.destDao = new UnitGroupDao(conf.target());
		this.refs = new RefSwitcher(conf);
		this.seq = conf.seq();
	}

	/// Returns the default flow property links that need to be applied after the
	/// import of flow properties.
	public List<DefaultLink> getDefaultLinks() {
		return defaultLinks;
	}

	public void run() {
		log.trace("import unit groups");
		try {
			for (var src : srcDao.getDescriptors()) {
				if (seq.isMapped(ModelType.UNIT_GROUP, src.id)) {
					syncUnitGroup(src);
				} else {
					createUnitGroup(src);
				}
			}
		} catch (Exception e) {
			log.error("failed to import unit groups", e);
		}
	}

	private void syncUnitGroup(UnitGroupDescriptor d) {
		UnitGroup src = srcDao.getForId(d.id);
		UnitGroup dest = destDao.getForId(seq.get(ModelType.UNIT_GROUP, d.id));
		boolean updated = false;
		for (Unit srcUnit : src.units) {
			Unit destUnit = dest.getUnit(srcUnit.name);
			if (destUnit != null)
				continue;
			destUnit = srcUnit.copy();
			destUnit.refId = srcUnit.refId;
			dest.units.add(destUnit);
			updated = true;
		}
		if (updated) {
			dest.lastChange = System.currentTimeMillis();
			dest.version += 1;
			dest = destDao.update(dest);
			log.info("updated unit group {}", dest);
		}
	}

	private void createUnitGroup(UnitGroupDescriptor d) {
		UnitGroup src = srcDao.getForId(d.id);
		UnitGroup dest = src.copy();
		dest.refId = src.refId;
		switchUnitRefIds(src, dest);
		dest.defaultFlowProperty = null;
		dest.category = refs.switchRef(src.category);
		dest = destDao.insert(dest);
		seq.put(ModelType.UNIT_GROUP, src.id, dest.id);
		if (src.defaultFlowProperty != null) {
			defaultLinks.add(new DefaultLink(dest, src.defaultFlowProperty.id));
		}
	}

	private void switchUnitRefIds(UnitGroup srcGroup, UnitGroup destGroup) {
		for (Unit srcUnit : srcGroup.units) {
			Unit destUnit = destGroup.getUnit(srcUnit.name);
			if (destUnit == null)
				continue;
			destUnit.refId = srcUnit.refId;
		}
	}

	/// Stores the default flow property link for a unit group. We need to set
	/// the link after the import of flow properties.
	record DefaultLink(UnitGroup targetUnitGroup, long sourcePropertyId) {
	}

}
