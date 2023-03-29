package org.openlca.io.olca;

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
	private final Seq seq;

	ImpactCategoryImport(Config conf) {
		sourceDao = new ImpactCategoryDao(conf.source());
		destDao = new ImpactCategoryDao(conf.target());
		this.refs = new RefSwitcher(conf);
		this.seq = conf.seq();
	}

	void run() {
		log.trace("import LCIA categories");
		for (ImpactDescriptor d : sourceDao.getDescriptors()) {
			if (seq.contains(Seq.IMPACT_CATEGORY, d.refId))
				continue;
			ImpactCategory src = sourceDao.getForId(d.id);
			ImpactCategory dest = src.copy();
			dest.refId = src.refId;
			dest.category = refs.switchRef(src.category);
			switchFactorRefs(dest);
			dest = destDao.insert(dest);
			seq.put(Seq.IMPACT_CATEGORY, src.refId, dest.id);
		}
	}

	private void switchFactorRefs(ImpactCategory impact) {
		for (ImpactFactor f : impact.impactFactors) {
			// TODO: swap locations
			f.flow = refs.switchRef(f.flow);
			f.unit = refs.switchRef(f.unit);
			f.flowPropertyFactor = refs.switchRef(f.flowPropertyFactor, f.flow);
		}
	}
}
