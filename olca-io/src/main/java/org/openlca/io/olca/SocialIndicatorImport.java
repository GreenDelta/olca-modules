package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SocialIndicatorImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private SocialIndicatorDao srcDao;
	private SocialIndicatorDao destDao;
	private RefSwitcher refs;
	private Sequence seq;

	SocialIndicatorImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new SocialIndicatorDao(source);
		this.destDao = new SocialIndicatorDao(dest);
		this.refs = new RefSwitcher(source, dest, seq);
		this.seq = seq;
	}

	public void run() {
		log.trace("import social indicators");
		try {
			for (SocialIndicatorDescriptor descriptor : srcDao.getDescriptors()) {
				if (seq.contains(seq.SOCIAL_INDICATOR, descriptor.getRefId()))
					continue;
				createSocialIndicator(descriptor);
			}
		} catch (Exception e) {
			log.error("failed to import social indicators", e);
		}
	}

	private void createSocialIndicator(SocialIndicatorDescriptor descriptor) {
		SocialIndicator src = srcDao.getForId(descriptor.getId());
		SocialIndicator dest = src.clone();
		dest.setRefId(src.getRefId());
		dest.setCategory(refs.switchRef(src.getCategory()));
		dest.activityQuantity = refs.switchRef(src.activityQuantity);
		dest.activityUnit = refs.switchRef(src.activityUnit);
		dest = destDao.insert(dest);
		seq.put(seq.ACTOR, src.getRefId(), dest.getId());
	}

}
