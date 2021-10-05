package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProjectImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ProjectDao srcDao;
	private final ProjectDao destDao;
	private final RefSwitcher refs;
	private final Sequence seq;

	ProjectImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ProjectDao(source);
		this.destDao = new ProjectDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
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
		Project destProject = srcProject.copy();
		destProject.refId = srcProject.refId;
		destProject.category = refs.switchRef(srcProject.category);
		destProject.impactMethod = refs.switchRef(srcProject.impactMethod);
		destProject.nwSet = refs.switchRef(srcProject.nwSet);
		for (ProjectVariant variant : destProject.variants)
			switchVariantReferences(variant);
		destProject = destDao.insert(destProject);
		seq.put(seq.PROJECT, srcProject.refId, destProject.id);
	}

	private void switchVariantReferences(ProjectVariant variant) {
		variant.productSystem = refs.switchRef(variant.productSystem);
		variant.unit = refs.switchRef(variant.unit);
		switchVariantProperty(variant);
		for (ParameterRedef redef : variant.parameterRedefs) {
			if (redef.contextId == null)
				continue;
			if (redef.contextType == ModelType.IMPACT_METHOD) {
				redef.contextId = refs.getDestImpactMethodId(redef.contextId);
			} else {
				redef.contextId = refs.getDestProcessId(redef.contextId);
			}
		}
	}

	private void switchVariantProperty(ProjectVariant variant) {
		if (variant.flowPropertyFactor == null)
			return;
		ProductSystem destSystem = variant.productSystem;
		if (destSystem == null || destSystem.referenceExchange == null) {
			variant.flowPropertyFactor = null;
			return;
		}
		Flow destFlow = destSystem.referenceExchange.flow;
		variant.flowPropertyFactor = refs.switchRef(
		variant.flowPropertyFactor, destFlow);
	}
}
