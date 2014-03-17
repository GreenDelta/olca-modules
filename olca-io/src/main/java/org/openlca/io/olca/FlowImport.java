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
				if (seq.contains(seq.FLOW, descriptor.getRefId()))
					continue;
				createFlow(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import flows", e);
		}
	}

	private void createFlow(FlowDescriptor descriptor) {
		Flow srcFlow = sourceDao.getForId(descriptor.getId());
		Flow destFlow = srcFlow.clone();
		destFlow.setRefId(srcFlow.getRefId());
		destFlow.setCategory(refs.switchRef(srcFlow.getCategory()));
		destFlow.setLocation(refs.switchRef(srcFlow.getLocation()));
		switchFlowProperties(srcFlow, destFlow);
		destFlow = destDao.insert(destFlow);
		seq.put(seq.FLOW, srcFlow.getRefId(), destFlow.getId());
	}

	private void switchFlowProperties(Flow srcFlow, Flow destFlow) {
		destFlow.setReferenceFlowProperty(refs.switchRef(srcFlow
				.getReferenceFlowProperty()));
		for (FlowPropertyFactor fac : destFlow.getFlowPropertyFactors()) {
			fac.setFlowProperty(refs.switchRef(fac.getFlowProperty()));
		}
	}

}
