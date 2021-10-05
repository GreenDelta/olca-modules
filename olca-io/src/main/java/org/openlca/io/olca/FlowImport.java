package org.openlca.io.olca;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private FlowDao sourceDao;
	private FlowDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	FlowImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new FlowDao(source);
		this.destDao = new FlowDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import flows");
		try {
			for (FlowDescriptor descriptor : sourceDao.getDescriptors()) {
				if (seq.contains(seq.FLOW, descriptor.refId))
					continue;
				createFlow(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import flows", e);
		}
	}

	private void createFlow(FlowDescriptor descriptor) {
		Flow srcFlow = sourceDao.getForId(descriptor.id);
		Flow destFlow = srcFlow.copy();
		destFlow.refId = srcFlow.refId;
		destFlow.category = refs.switchRef(srcFlow.category);
		destFlow.location = refs.switchRef(srcFlow.location);
		switchFlowProperties(srcFlow, destFlow);
		destFlow = destDao.insert(destFlow);
		seq.put(seq.FLOW, srcFlow.refId, destFlow.id);
	}

	private void switchFlowProperties(Flow srcFlow, Flow destFlow) {
		destFlow.referenceFlowProperty = refs.switchRef(srcFlow.referenceFlowProperty);
		for (FlowPropertyFactor fac : destFlow.flowPropertyFactors) {
			fac.flowProperty = refs.switchRef(fac.flowProperty);
		}
	}

}
