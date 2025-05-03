package org.openlca.io.olca;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RefEntityDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;

/**
 * We copy most of the entities from the source database to the target database
 * by cloning the source entity, changing the references of the source entity to
 * the corresponding references in the destination database, and finally
 * inserting the new entity in the destination database. This class helps to
 * switch a referenced entity from the source database to the corresponding
 * entity in the destination database.
 */
class RefSwitcher {

	private final SeqMap seq;
	private final IDatabase target;

	RefSwitcher(Config conf) {
		this.target = conf.target();
		this.seq = conf.seq();
	}

	Category switchRef(Category srcCategory) {
		return switchRef(ModelType.CATEGORY, new CategoryDao(target), srcCategory);
	}

	private <T extends RefEntity> T switchRef(
			ModelType type, RefEntityDao<T, ?> dao, T srcEntity) {
		if (srcEntity == null)
			return null;
		long id = seq.get(type, srcEntity.id);
		if (id == 0)
			return null;
		return dao.getForId(id);
	}

	/**
	 * Returns the corresponding flow property factor of the destination flow.
	 */
	FlowPropertyFactor switchRef(FlowPropertyFactor srcFactor, Flow destFlow) {
		if (srcFactor == null || destFlow == null)
			return null;
		FlowProperty srcProp = srcFactor.flowProperty;
		if (srcProp == null)
			return null;
		long propId = seq.get(ModelType.FLOW_PROPERTY, srcProp.id);
		for (FlowPropertyFactor fac : destFlow.flowPropertyFactors) {
			if (fac.flowProperty == null)
				continue;
			if (propId == fac.flowProperty.id)
				return fac;
		}
		return null;
	}


}
