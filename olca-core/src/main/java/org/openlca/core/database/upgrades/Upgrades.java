package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for checking the version of a database and upgrading
 * databases.
 */
public class Upgrades {

	private final IUpgrade[] upgrades = {
			new Upgrade3(),
			new Upgrade4(),
			new Upgrade5(),
			new Upgrade6(),
			new Upgrade7(),
			new Upgrade8(),
			new Upgrade9(),
	};

	private final Logger log = LoggerFactory.getLogger(Upgrades.class);

	private Upgrades() {
	}

	public static void on(IDatabase dbDatabase) {
		Upgrades upgrades = new Upgrades();
		upgrades.run(dbDatabase);
	}

	private void run(IDatabase db) {
		IUpgrade next;
		while ((next = findNextUpgrade(db)) != null) {
			log.info("execute update from v({}) to v{}",
					next.getInitialVersions(),
					next.getEndVersion());
			next.exec(db);
			DbUtil.setVersion(db, next.getEndVersion());
		}
		db.clearCache();
		log.debug("no more upgrades");
	}

	private IUpgrade findNextUpgrade(IDatabase db) {
		int version = db.getVersion();
		for (IUpgrade upgrade : upgrades) {
			for (int v : upgrade.getInitialVersions()) {
				if (v == version)
					return upgrade;
			}
		}
		return null;
	}
}
