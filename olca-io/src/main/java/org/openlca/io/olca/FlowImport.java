package org.openlca.io.olca;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private FlowDao sourceDao;
	private FlowDao destDao;
	private CategoryDao destCategoryDao;
	private LocationDao destLocationDao;
	private FlowPropertyDao destPropertyDao;
	private Sequence seq;

	FlowImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new FlowDao(source);
		this.destDao = new FlowDao(dest);
		this.destCategoryDao = new CategoryDao(dest);
		this.destLocationDao = new LocationDao(dest);
		this.destPropertyDao = new FlowPropertyDao(dest);
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
		if (srcFlow.getCategory() != null) {
			long catId = seq.get(seq.CATEGORY, srcFlow.getCategory().getRefId());
			destFlow.setCategory(destCategoryDao.getForId(catId));
		}
		if (srcFlow.getLocation() != null) {
			long locId = seq.get(seq.LOCATION, srcFlow.getLocation().getRefId());
			destFlow.setLocation(destLocationDao.getForId(locId));
		}
		switchFlowProperties(srcFlow, destFlow);
		destFlow = destDao.insert(destFlow);
		seq.put(seq.FLOW, srcFlow.getRefId(), destFlow.getId());
	}

	private void switchFlowProperties(Flow srcFlow, Flow destFlow) {
		FlowProperty refProp = srcFlow.getReferenceFlowProperty();
		if (refProp != null) {
			long propId = seq.get(seq.FLOW_PROPERTY, refProp.getRefId());
			destFlow.setReferenceFlowProperty(destPropertyDao.getForId(propId));
		}
		for (FlowPropertyFactor fac : destFlow.getFlowPropertyFactors()) {
			if (fac.getFlowProperty() == null)
				continue;
			FlowProperty prop = fac.getFlowProperty();
			long propId = seq.get(seq.FLOW_PROPERTY, prop.getRefId());
			fac.setFlowProperty(destPropertyDao.getForId(propId));
		}
	}

}
