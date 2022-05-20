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
			new Upgrade03(),
			new Upgrade04(),
			new Upgrade05(),
			new Upgrade06(),
			new Upgrade07(),
			new Upgrade08(),
			new Upgrade09(),
			new Upgrade10(),
			new Upgrade11(),
	};

	private final Logger log = LoggerFactory.getLogger(Upgrades.class);

	private Upgrades() {
	}

	public static void on(IDatabase db) {
		new Upgrades().run(db);
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
