package org.openlca.io.olca;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.RefEntityDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Unit;

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
	private final IDatabase source;
	private final IDatabase target;

	RefSwitcher(Config conf) {
		this.source = conf.source();
		this.target = conf.target();
		this.seq = conf.seq();
	}

	Category switchRef(Category srcCategory) {
		return switchRef(ModelType.CATEGORY, new CategoryDao(target), srcCategory);
	}

	Unit switchRef(Unit srcUnit) {
		if (srcUnit == null)
			return null;
		long id = seq.get(SeqMap.UNIT, srcUnit.refId);
		if (id == 0)
			return null;
		UnitDao dao = new UnitDao(target);
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

	private <T extends RefEntity> T switchRef(
			ModelType type, RefEntityDao<T, ?> dao, T srcEntity) {
		if (srcEntity == null)
			return null;
		long id = seq.get(type, srcEntity.id);
		if (id == 0)
			return null;
		return dao.getForId(id);
	}

	Long getDestImpactId(Long srcId) {
		if (srcId == null)
			return null;
		var dao = new ImpactCategoryDao(source);
		var d = dao.getDescriptor(srcId);
		return d != null
				? seq.get(SeqMap.IMPACT_METHOD, d.refId)
				: null;
	}

	Long getDestProcessId(Long srcId) {
		if (srcId == null)
			return null;
		var dao = new ProcessDao(source);
		var d = dao.getDescriptor(srcId);
		return d != null
				? seq.get(SeqMap.PROCESS, d.refId)
				: null;
	}

}
