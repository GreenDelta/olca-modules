package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import the data from one openLCA database into another database.
 */
public class DatabaseImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private IDatabase source;
	private IDatabase dest;

	public DatabaseImport(IDatabase source, IDatabase destination) {
		this.source = source;
		this.dest = destination;
	}

	@Override
	public void run() {
		log.trace("run database import from {} to {}", source, dest);
		try {
			Sequence seq = new Sequence(dest);
			new LocationImport(source, dest, seq).run();
			new CategoryImport(source, dest, seq).run();
			new ActorImport(source, dest, seq).run();
			new SourceImport(source, dest, seq).run();
		} catch (Exception e) {
			log.error("Database import failed", e);
		}
	}
}
