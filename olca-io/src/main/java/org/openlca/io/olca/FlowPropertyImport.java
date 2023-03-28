package org.openlca.io.olca;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowPropertyImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final FlowPropertyDao sourceDao;
	private final FlowPropertyDao destDao;
	private final RefSwitcher refs;
	private final Seq seq;

	FlowPropertyImport(IDatabase source, IDatabase dest, Seq seq) {
		this.sourceDao = new FlowPropertyDao(source);
		this.destDao = new FlowPropertyDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import flow properties");
		try {
			for (var d : sourceDao.getDescriptors()) {
				if (seq.contains(seq.FLOW_PROPERTY, d.refId))
					continue;
				createFlowProperty(d);
			}
		} catch (Exception e) {
			log.error("failed to import flow properties", e);
		}
	}

	private void createFlowProperty(FlowPropertyDescriptor d) {
		var srcProp = sourceDao.getForId(d.id);
		var destProp = srcProp.copy();
		destProp.refId = srcProp.refId;
		destProp.unitGroup = refs.switchRef(srcProp.unitGroup);
		destProp.category = refs.switchRef(srcProp.category);
		destProp = destDao.insert(destProp);
		seq.put(seq.FLOW_PROPERTY, srcProp.refId, destProp.id);
	}
}
