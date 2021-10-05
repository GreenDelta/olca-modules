package org.openlca.io.olca;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ActorImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private ActorDao sourceDao;
	private ActorDao destDao;
	private Sequence seq;
	private RefSwitcher refs;

	ActorImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.sourceDao = new ActorDao(source);
		this.destDao = new ActorDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import actors");
		try {
			for (ActorDescriptor descriptor : sourceDao.getDescriptors()) {
				if (seq.contains(seq.ACTOR, descriptor.refId))
					continue;
				createActor(descriptor);
			}
		} catch (Exception e) {
			log.error("Actor import failed", e);
		}
	}

	private void createActor(ActorDescriptor descriptor) {
		Actor srcActor = sourceDao.getForId(descriptor.id);
		Actor destActor = srcActor.copy();
		destActor.refId = srcActor.refId;
		destActor.category = refs.switchRef(srcActor.category);
		destActor = destDao.insert(destActor);
		seq.put(seq.ACTOR, srcActor.refId, destActor.id);
	}
}
