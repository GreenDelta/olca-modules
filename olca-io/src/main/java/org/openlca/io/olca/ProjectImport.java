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
				if (seq.contains(seq.PROJECT, descriptor.getRefId()))
					continue;
				createProject(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import projects", e);
		}
	}

	private void createProject(ProjectDescriptor descriptor) {
		Project srcProject = srcDao.getForId(descriptor.getId());
		Project destProject = srcProject.clone();
		destProject.setRefId(srcProject.getRefId());
		destProject.setCategory(refs.switchRef(srcProject.getCategory()));
		destProject.setAuthor(refs.switchRef(srcProject.getAuthor()));
		switchImpactMethod(destProject);
		switchNwSet(destProject);
		for (ProjectVariant variant : destProject.getVariants())
			switchVariantReferences(variant);
		destProject = destDao.insert(destProject);
		seq.put(seq.PROJECT, srcProject.getRefId(), destProject.getId());
	}

	private void switchImpactMethod(Project destProject) {
		if (destProject.getImpactMethodId() == null)
			return;
		ImpactMethodDao srcDao = new ImpactMethodDao(source);
		ImpactMethodDescriptor descriptor = srcDao.getDescriptor(destProject
				.getImpactMethodId());
		if (descriptor == null) {
			destProject.setImpactMethodId(null);
			return;
		}
		long id = seq.get(seq.IMPACT_METHOD, descriptor.getRefId());
		destProject.setImpactMethodId(id);
	}

	private void switchNwSet(Project destProject) {
		if (destProject.getNwSetId() == null)
			return;
		if (destProject.getImpactMethodId() == null) {
			destProject.setNwSetId(null);
			return;
		}
		NwSetDao srcDao = new NwSetDao(source);
		NwSetDescriptor descriptor = srcDao.getDescriptor(destProject
				.getNwSetId());
		if (descriptor == null) {
			destProject.setNwSetId(null);
			return;
		}
		long id = seq.get(seq.NW_SET, descriptor.getRefId());
		destProject.setNwSetId(id);
	}

	private void switchVariantReferences(ProjectVariant variant) {
		variant.setProductSystem(refs.switchRef(variant.getProductSystem()));
		variant.setUnit(refs.switchRef(variant.getUnit()));
		switchVariantProperty(variant);
		for (ParameterRedef redef : variant.getParameterRedefs()) {
			if (redef.getContextId() == null)
				continue;
			if (redef.getContextType() == ModelType.IMPACT_METHOD) {
				Long destMethodId = refs.getDestImpactMethodId(redef
						.getContextId());
				redef.setContextId(destMethodId);
			} else {
				Long destProcessId = refs
						.getDestProcessId(redef.getContextId());
				redef.setContextId(destProcessId);
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
