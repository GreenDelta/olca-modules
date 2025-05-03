package org.openlca.io.olca;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;

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

	RefSwitcher(Config conf) {
		this.seq = conf.seq();
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
