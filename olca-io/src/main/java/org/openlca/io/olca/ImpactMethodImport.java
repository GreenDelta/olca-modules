package org.openlca.io.olca;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactMethodImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ImpactMethodDao srcDao;
	private ImpactMethodDao destDao;
	private IDatabase dest;
	private Sequence seq;

	ImpactMethodImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new ImpactMethodDao(source);
		this.destDao = new ImpactMethodDao(dest);
		this.dest = dest;
		this.seq = seq;
	}

	public void run() {
		log.trace("import LCIA methods");
		try {
			for (ImpactMethodDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.IMPACT_METHOD, descriptor.getRefId()))
					continue;
				createMethod(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import LCIA methods", e);
		}
	}

	private void createMethod(ImpactMethodDescriptor descriptor) {
		ImpactMethod srcMethod = srcDao.getForId(descriptor.getId());
		ImpactMethod destMethod = srcMethod.clone();
		destMethod.setRefId(srcMethod.getRefId());
		switchCategory(srcMethod, destMethod);
		switchFactorReferences(destMethod);
		// we need to set the reference IDs from the source as they are generated
		// new in the clone method.
		switchImpactRefIds(srcMethod, destMethod);
		switchNwSetRefIds(srcMethod, destMethod);
		destMethod = destDao.insert(destMethod);
		seq.put(seq.IMPACT_METHOD, srcMethod.getRefId(), destMethod.getId());
	}

	private void switchFactorReferences(ImpactMethod destMethod) {
		for (ImpactCategory category : destMethod.getImpactCategories()) {
			for (ImpactFactor factor : category.getImpactFactors()) {
				switchFlow(factor);
				switchFlowProperty(factor);
				switchUnit(factor);
			}
		}
	}

	private void switchFlow(ImpactFactor factor) {
		Flow srcFlow = factor.getFlow();
		if (srcFlow == null)
			return;
		long id = seq.get(seq.FLOW, srcFlow.getRefId());
		FlowDao dao = new FlowDao(dest);
		factor.setFlow(dao.getForId(id));
	}

	private void switchFlowProperty(ImpactFactor factor) {
		Flow destFlow = factor.getFlow(); // flow must be switched already
		FlowPropertyFactor srcPropFactor = factor.getFlowPropertyFactor();
		if (destFlow == null || srcPropFactor == null ||
				srcPropFactor.getFlowProperty() == null)
			return;
		FlowProperty srcProperty = srcPropFactor.getFlowProperty();
		FlowPropertyFactor destPropFactor = null;
		for (FlowPropertyFactor fac : destFlow.getFlowPropertyFactors()) {
			if (fac.getFlowProperty() == null)
				continue;
			if (Strings.nullOrEqual(fac.getFlowProperty().getRefId(),
					srcProperty.getRefId())) {
				destPropFactor = fac;
				break;
			}
		}
		factor.setFlowPropertyFactor(destPropFactor);
	}

	private void switchUnit(ImpactFactor factor) {
		Unit srcUnit = factor.getUnit();
		if (srcUnit == null)
			return;
		long id = seq.get(seq.UNIT, srcUnit.getRefId());
		BaseDao<Unit> dao = dest.createDao(Unit.class);
		factor.setUnit(dao.getForId(id));
	}

	private void switchCategory(ImpactMethod srcMethod, ImpactMethod destMethod) {
		if (srcMethod.getCategory() == null)
			return;
		long id = seq.get(seq.CATEGORY, srcMethod.getCategory().getRefId());
		CategoryDao destDao = new CategoryDao(dest);
		destMethod.setCategory(destDao.getForId(id));
	}

	private void switchImpactRefIds(ImpactMethod srcMethod, ImpactMethod destMethod) {
		for (ImpactCategory srcCat : srcMethod.getImpactCategories()) {
			for (ImpactCategory destCat : destMethod.getImpactCategories()) {
				if (areEqual(srcCat, destCat)) {
					destCat.setRefId(srcCat.getRefId());
					break;
				}
			}
		}
	}

	private boolean areEqual(ImpactCategory srcCat, ImpactCategory destCat) {
		return Strings.nullOrEqual(srcCat.getName(), destCat.getName())
				&& Strings.nullOrEqual(srcCat.getReferenceUnit(),
				destCat.getReferenceUnit())
				&& Strings.nullOrEqual(srcCat.getDescription(),
				destCat.getDescription())
				&& (srcCat.getImpactFactors().size() == destCat
				.getImpactFactors().size());
	}

	private void switchNwSetRefIds(ImpactMethod srcMethod, ImpactMethod destMethod) {
		for (NwSet srcNwSet : srcMethod.getNwSets()) {
			for (NwSet destNwSet : destMethod.getNwSets()) {
				if (areEqual(srcNwSet, destNwSet)) {
					destNwSet.setRefId(srcNwSet.getRefId());
					break;
				}
			}
		}
	}

	private boolean areEqual(NwSet srcNwSet, NwSet destNwSet) {
		return Strings.nullOrEqual(srcNwSet.getName(), destNwSet.getName())
				&& Strings.nullOrEqual(srcNwSet.getDescription(), destNwSet.getName())
				&& Strings.nullOrEqual(srcNwSet.getWeightedScoreUnit(),
				destNwSet.getWeightedScoreUnit());
	}

}
