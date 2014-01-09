package org.openlca.io.olca;

import org.openlca.core.database.CategoryDao;
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
	private CategoryDao destCategoryDao;
	private Sequence seq;

	SourceImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new SourceDao(source);
		this.destDao = new SourceDao(dest);
		this.destCategoryDao = new CategoryDao(dest);
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
		if (srcSource.getCategory() != null) {
			long catId = seq.get(seq.CATEGORY, srcSource.getCategory().getRefId());
			destSource.setCategory(destCategoryDao.getForId(catId));
		}
		destSource = destDao.insert(destSource);
		seq.put(seq.SOURCE, srcSource.getRefId(), destSource.getId());
	}

}
