package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LocationImport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final LocationDao srcDao;
	private final LocationDao destDao;
	private final Sequence seq;

	public LocationImport(IDatabase source, IDatabase dest, Sequence seq) {
		this.srcDao = new LocationDao(source);
		this.destDao = new LocationDao(dest);
		this.seq = seq;
	}

	public void run() {
		log.trace("import locations");
		try {
			for (Location srcLoc : srcDao.getAll()) {
				if (seq.contains(seq.LOCATION, srcLoc.refId))
					continue;
				Location destLoc = srcLoc.copy();
				destLoc.refId = srcLoc.refId;
				destLoc = destDao.insert(destLoc);
				seq.put(seq.LOCATION, srcLoc.refId, destLoc.id);
			}
		} catch (Exception e) {
			log.error("failed to import locations", e);
		}
	}

}
