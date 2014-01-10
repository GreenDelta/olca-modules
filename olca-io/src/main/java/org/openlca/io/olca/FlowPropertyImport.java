package org.openlca.io.olca;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private FlowPropertyDao sourceDao;
	private FlowPropertyDao destDao;
	private UnitGroupDao destUnitGroupDao;
	private CategoryDao destCategoryDao;
	private Sequence seq;

	FlowPropertyImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new FlowPropertyDao(source);
		this.destUnitGroupDao = new UnitGroupDao(dest);
		this.destDao = new FlowPropertyDao(dest);
		this.destCategoryDao = new CategoryDao(dest);
		this.seq = seq;
	}

	public void run() {
		log.trace("import flow properties");
		try {
			for (FlowPropertyDescriptor descriptor : sourceDao.getDescriptors()) {
				if (seq.contains(seq.FLOW_PROPERTY, descriptor.getRefId()))
					continue;
				createFlowProperty(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import flow properties");
		}
	}

	private void createFlowProperty(FlowPropertyDescriptor descriptor) {
		FlowProperty srcProp = sourceDao.getForId(descriptor.getId());
		FlowProperty destProp = srcProp.clone();
		destProp.setRefId(srcProp.getRefId());
		if (srcProp.getUnitGroup() != null) {
			long unitGroupId = seq.get(seq.UNIT_GROUP,
					srcProp.getUnitGroup().getRefId());
			destProp.setUnitGroup(destUnitGroupDao.getForId(unitGroupId));
		}
		if (srcProp.getCategory() != null) {
			long catId = seq.get(seq.CATEGORY, srcProp.getCategory().getRefId());
			destProp.setCategory(destCategoryDao.getForId(catId));
		}
		destProp = destDao.insert(destProp);
		seq.put(seq.FLOW_PROPERTY, srcProp.getRefId(), destProp.getId());
	}


}
