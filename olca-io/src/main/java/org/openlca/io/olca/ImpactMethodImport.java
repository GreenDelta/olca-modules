package org.openlca.io.olca;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwFactor;
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

	private void createMethod(ImpactMethodDescriptor d) {
		ImpactMethod src = srcDao.getForId(d.id);
		ImpactMethod dest = src.copy();
		dest.refId = src.refId;
		dest.category = refs.switchRef(src.category);
		switchImpacts(dest);
		switchNwSets(src, dest);
		dest = destDao.insert(dest);
		seq.put(seq.IMPACT_METHOD, src.refId, dest.id);
		for (NwSet nwSet : dest.nwSets) {
			seq.put(seq.NW_SET, nwSet.refId, nwSet.id);
		}
	}


	private void switchImpacts(ImpactMethod dest) {
		List<ImpactCategory> switched = new ArrayList<>(
				dest.impactCategories.size());
		for (ImpactCategory srcCat : dest.impactCategories) {
			ImpactCategory destCat = refs.switchRef(srcCat);
			if (destCat != null) {
				switched.add(destCat);
			}
		}
		dest.impactCategories.clear();
		dest.impactCategories.addAll(switched);
	}

	private void switchNwSets(
			ImpactMethod srcMethod, ImpactMethod destMethod) {
		for (NwSet destNwSet : destMethod.nwSets) {
			for (NwFactor f : destNwSet.factors) {
				f.impactCategory = refs.switchRef(f.impactCategory);
			}
			for (NwSet srcNwSet : srcMethod.nwSets) {
				// we need to set the reference IDs from the source as they are
				// generated new in the clone method.
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
