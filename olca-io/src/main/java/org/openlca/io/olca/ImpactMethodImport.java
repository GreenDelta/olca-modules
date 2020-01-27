package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactMethodImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ImpactMethodDao srcDao;
	private ImpactMethodDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	ImpactMethodImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ImpactMethodDao(source);
		this.destDao = new ImpactMethodDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import LCIA methods");
		try {
			for (ImpactMethodDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.IMPACT_METHOD, descriptor.refId))
					continue;
				createMethod(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import LCIA methods", e);
		}
	}

	private void createMethod(ImpactMethodDescriptor descriptor) {
		ImpactMethod srcMethod = srcDao.getForId(descriptor.id);
		ImpactMethod destMethod = srcMethod.clone();
		destMethod.refId = srcMethod.refId;
		destMethod.category = refs.switchRef(srcMethod.category);
		switchFactorReferences(destMethod);
		// we need to set the reference IDs from the source as they are
		// generated
		// new in the clone method.
		switchImpactRefIds(srcMethod, destMethod);
		switchNwSetRefIds(srcMethod, destMethod);
		destMethod = destDao.insert(destMethod);
		seq.put(seq.IMPACT_METHOD, srcMethod.refId, destMethod.id);
		for (NwSet nwSet : destMethod.nwSets)
			seq.put(seq.NW_SET, nwSet.refId, nwSet.id);
	}

	private void switchFactorReferences(ImpactMethod destMethod) {
		for (ImpactCategory category : destMethod.impactCategories) {
			for (ImpactFactor factor : category.impactFactors) {
				factor.flow = refs.switchRef(factor.flow);
				factor.unit = refs.switchRef(factor.unit);
				Flow destFlow = factor.flow; // already switched
				factor.flowPropertyFactor = refs.switchRef(
				factor.flowPropertyFactor, destFlow);
			}
		}
	}

	private void switchImpactRefIds(ImpactMethod srcMethod,
			ImpactMethod destMethod) {
		for (ImpactCategory srcCat : srcMethod.impactCategories) {
			for (ImpactCategory destCat : destMethod.impactCategories) {
				if (areEqual(srcCat, destCat)) {
					destCat.refId = srcCat.refId;
					break;
				}
			}
		}
	}

	private boolean areEqual(ImpactCategory srcCat, ImpactCategory destCat) {
		return Strings.nullOrEqual(srcCat.name, destCat.name)
				&& Strings.nullOrEqual(srcCat.referenceUnit,
						destCat.referenceUnit)
				&& Strings.nullOrEqual(srcCat.description,
						destCat.description)
				&& (srcCat.impactFactors.size() == destCat.impactFactors.size());
	}

	private void switchNwSetRefIds(ImpactMethod srcMethod,
			ImpactMethod destMethod) {
		for (NwSet srcNwSet : srcMethod.nwSets) {
			for (NwSet destNwSet : destMethod.nwSets) {
				if (areEqual(srcNwSet, destNwSet)) {
					destNwSet.refId = srcNwSet.refId;
					break;
				}
			}
		}
	}

	private boolean areEqual(NwSet srcNwSet, NwSet destNwSet) {
		return Strings.nullOrEqual(srcNwSet.name, destNwSet.name)
				&& Strings.nullOrEqual(srcNwSet.description,
						destNwSet.description)
				&& Strings.nullOrEqual(srcNwSet.weightedScoreUnit,
						destNwSet.weightedScoreUnit);
	}

}
