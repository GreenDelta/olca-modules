package org.openlca.io.olca;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.descriptors.DQSystemDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DQSystemImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private DQSystemDao srcDao;
	private DQSystemDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	DQSystemImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new DQSystemDao(source);
		this.destDao = new DQSystemDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import data quality systems");
		try {
			for (DQSystemDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.DQ_SYSTEM, descriptor.getRefId()))
					continue;
				create(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import data quality systems", e);
		}
	}

	private void create(DQSystemDescriptor descriptor) {
		DQSystem src = srcDao.getForId(descriptor.getId());
		DQSystem dest = src.clone();
		dest.setRefId(src.getRefId());
		dest.setCategory(refs.switchRef(src.getCategory()));
		dest.source = refs.switchRef(src.source);
		dest = destDao.insert(dest);
		seq.put(seq.DQ_SYSTEM, src.getRefId(), dest.getId());
	}

}
