package org.openlca.io.olca;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ActorImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ActorDao sourceDao;
	private ActorDao destDao;
	private IDatabase dest;
	private Sequence seq;

	ActorImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new ActorDao(source);
		this.destDao = new ActorDao(dest);
		this.dest = dest;
		this.seq = seq;
	}

	public void run() {
		try {
			CategoryDao destCategoryDao = new CategoryDao(dest);
			for (ActorDescriptor descriptor : sourceDao.getDescriptors()) {
				if (seq.contains(seq.ACTOR, descriptor.getRefId()))
					continue;
				createActor(destCategoryDao, descriptor);
			}
		} catch (Exception e) {
			log.error("Actor import failed", e);
		}
	}

	private void createActor(CategoryDao destCategoryDao,
	                         ActorDescriptor descriptor) {
		Actor srcActor = sourceDao.getForId(descriptor.getId());
		Actor destActor = srcActor.clone();
		if (srcActor.getCategory() != null) {
			long catId = seq.get(seq.ACTOR, srcActor.getCategory().getRefId());
			destActor.setCategory(destCategoryDao.getForId(catId));
		}
		destDao.insert(destActor);
	}
}
