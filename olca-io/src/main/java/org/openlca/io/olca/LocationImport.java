package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private LocationDao srcDao;
	private LocationDao destDao;
	private Sequence seq;

	public LocationImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new LocationDao(source);
		this.destDao = new LocationDao(dest);
		this.seq = seq;
	}

	public void run() {
		log.trace("import locations");
		try {
			for (Location srcLoc : srcDao.getAll()) {
				if (seq.contains(seq.LOCATION, srcLoc.getRefId()))
					continue;
				Location destLoc = srcLoc.clone();
				destLoc.setRefId(srcLoc.getRefId());
				destLoc = destDao.insert(destLoc);
				seq.put(seq.LOCATION, srcLoc.getRefId(), destLoc.getId());
			}
		} catch (Exception e) {
			log.error("failed to import locations", e);
		}
	}

}
