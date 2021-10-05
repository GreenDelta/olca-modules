package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SourceDao;
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SourceImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final SourceDao srcDao;
	private final SourceDao destDao;
	private final RefSwitcher refs;
	private final Sequence seq;

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
				if (seq.contains(seq.SOURCE, descriptor.refId))
					continue;
				createSource(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import sources", e);
		}
	}

	private void createSource(SourceDescriptor descriptor) {
		Source srcSource = srcDao.getForId(descriptor.id);
		Source destSource = srcSource.copy();
		destSource.refId = srcSource.refId;
		destSource.category = refs.switchRef(srcSource.category);
		destSource = destDao.insert(destSource);
		seq.put(seq.SOURCE, srcSource.refId, destSource.id);
	}

}
