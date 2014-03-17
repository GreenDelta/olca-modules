package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SourceImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private SourceDao srcDao;
	private SourceDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	SourceImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new SourceDao(source);
		this.destDao = new SourceDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import sources");
		try {
			for (SourceDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.SOURCE, descriptor.getRefId()))
					continue;
				createSource(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import sources", e);
		}
	}

	private void createSource(SourceDescriptor descriptor) {
		Source srcSource = srcDao.getForId(descriptor.getId());
		Source destSource = srcSource.clone();
		destSource.setRefId(srcSource.getRefId());
		destSource.setCategory(refs.switchRef(srcSource.getCategory()));
		destSource = destDao.insert(destSource);
		seq.put(seq.SOURCE, srcSource.getRefId(), destSource.getId());
	}

}
