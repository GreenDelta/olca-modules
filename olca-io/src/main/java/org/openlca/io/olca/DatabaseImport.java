package org.openlca.io.olca;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

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

			UnitGroupImport unitGroupImport = new UnitGroupImport(source, dest, seq);
			unitGroupImport.run();
			HashMap<String, UnitGroup> requirePropertyUpdate = unitGroupImport
					.getRequirePropertyUpdate();
			// TODO: update unit groups after flow property import if required

		} catch (Exception e) {
			log.error("Database import failed", e);
		}
	}
}
