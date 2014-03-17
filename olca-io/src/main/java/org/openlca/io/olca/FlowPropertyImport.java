package org.openlca.io.olca;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private FlowPropertyDao sourceDao;
	private FlowPropertyDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	FlowPropertyImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new FlowPropertyDao(source);
		this.destDao = new FlowPropertyDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
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
			log.error("failed to import flow properties", e);
		}
	}

	private void createFlowProperty(FlowPropertyDescriptor descriptor) {
		FlowProperty srcProp = sourceDao.getForId(descriptor.getId());
		FlowProperty destProp = srcProp.clone();
		destProp.setRefId(srcProp.getRefId());
		destProp.setUnitGroup(refs.switchRef(srcProp.getUnitGroup()));
		destProp.setCategory(refs.switchRef(srcProp.getCategory()));
		destProp = destDao.insert(destProp);
		seq.put(seq.FLOW_PROPERTY, srcProp.getRefId(), destProp.getId());
	}
}
