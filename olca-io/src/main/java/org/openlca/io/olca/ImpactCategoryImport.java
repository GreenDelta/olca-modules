package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImpactCategoryImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ImpactCategoryDao sourceDao;
	private final ImpactCategoryDao destDao;
	private final RefSwitcher refs;
	private final Sequence seq;

	ImpactCategoryImport(IDatabase source, IDatabase dest, Sequence seq) {
		sourceDao = new ImpactCategoryDao(source);
		destDao = new ImpactCategoryDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	void run() {
		log.trace("import LCIA categories");
		for (ImpactDescriptor d : sourceDao.getDescriptors()) {
			if (seq.contains(seq.IMPACT_CATEGORY, d.refId))
				continue;
			ImpactCategory src = sourceDao.getForId(d.id);
			ImpactCategory dest = src.copy();
			dest.refId = src.refId;
			dest.category = refs.switchRef(src.category);
			switchFactorRefs(dest);
			dest = destDao.insert(dest);
			seq.put(seq.IMPACT_CATEGORY, src.refId, dest.id);
		}
	}

	private void switchFactorRefs(ImpactCategory impact) {
		for (ImpactFactor f : impact.impactFactors) {
			f.flow = refs.switchRef(f.flow);
			f.unit = refs.switchRef(f.unit);
			f.flowPropertyFactor = refs.switchRef(
					f.flowPropertyFactor, f.flow);
		}
	}
}
