package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ProjectDao srcDao;
	private ProjectDao destDao;
	private IDatabase source;
	private RefSwitcher refs;
	private Sequence seq;

	ProjectImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProjectDao(source);
		this.destDao = new ProjectDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.source = source;
		this.seq = seq;
	}

	public void run() {
		log.trace("import projects");
		try {
			for (ProjectDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.PROJECT, descriptor.refId))
					continue;
				createProject(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import projects", e);
		}
	}

	private void createProject(ProjectDescriptor descriptor) {
		Project srcProject = srcDao.getForId(descriptor.id);
		Project destProject = srcProject.clone();
		destProject.setRefId(srcProject.getRefId());
		destProject.setCategory(refs.switchRef(srcProject.getCategory()));
		destProject.author = refs.switchRef(srcProject.author);
		switchImpactMethod(destProject);
		switchNwSet(destProject);
		for (ProjectVariant variant : destProject.variants)
			switchVariantReferences(variant);
		destProject = destDao.insert(destProject);
		seq.put(seq.PROJECT, srcProject.getRefId(), destProject.getId());
	}

	private void switchImpactMethod(Project destProject) {
		if (destProject.impactMethodId == null)
			return;
		ImpactMethodDao srcDao = new ImpactMethodDao(source);
		ImpactMethodDescriptor descriptor = srcDao.getDescriptor(destProject.impactMethodId);
		if (descriptor == null) {
			destProject.impactMethodId = null;
			return;
		}
		long id = seq.get(seq.IMPACT_METHOD, descriptor.refId);
		destProject.impactMethodId = id;
	}

	private void switchNwSet(Project destProject) {
		if (destProject.nwSetId == null)
			return;
		if (destProject.impactMethodId == null) {
			destProject.nwSetId = null;
			return;
		}
		NwSetDao srcDao = new NwSetDao(source);
		NwSetDescriptor descriptor = srcDao.getDescriptor(destProject.nwSetId);
		if (descriptor == null) {
			destProject.nwSetId = null;
			return;
		}
		long id = seq.get(seq.NW_SET, descriptor.refId);
		destProject.nwSetId = id;
	}

	private void switchVariantReferences(ProjectVariant variant) {
		variant.setProductSystem(refs.switchRef(variant.getProductSystem()));
		variant.setUnit(refs.switchRef(variant.getUnit()));
		switchVariantProperty(variant);
		for (ParameterRedef redef : variant.getParameterRedefs()) {
			if (redef.contextId == null)
				continue;
			if (redef.contextType == ModelType.IMPACT_METHOD) {
				Long destMethodId = refs.getDestImpactMethodId(redef.contextId);
				redef.contextId = destMethodId;
			} else {
				Long destProcessId = refs
						.getDestProcessId(redef.contextId);
				redef.contextId = destProcessId;
			}
		}
	}

	private void switchVariantProperty(ProjectVariant variant) {
		if (variant.getFlowPropertyFactor() == null)
			return;
		ProductSystem destSystem = variant.getProductSystem();
		if (destSystem == null || destSystem.referenceExchange == null) {
			variant.setFlowPropertyFactor(null);
			return;
		}
		Flow destFlow = destSystem.referenceExchange.flow;
		variant.setFlowPropertyFactor(refs.switchRef(
				variant.getFlowPropertyFactor(), destFlow));
	}
}
