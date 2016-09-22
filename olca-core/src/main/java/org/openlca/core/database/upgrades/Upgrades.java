package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for checking the version of a database and upgrading
 * databases.
 */
public class Upgrades {

	private final IUpgrade[] upgrades = {
			new Upgrade1(),
			new Upgrade2(),
			new Upgrade5(),
			new Upgrade3()
	};
	
	private Logger log = LoggerFactory.getLogger(Upgrades.class);

	private Upgrades() {
	}

	public static VersionState checkVersion(IDatabase database) {
		int version = database.getVersion();
		if (version < 1)
			return VersionState.ERROR;
		if (version == IDatabase.CURRENT_VERSION)
			return VersionState.CURRENT;
		if (version < IDatabase.CURRENT_VERSION)
			return VersionState.OLDER;
		else
			return VersionState.NEWER;
	}

	public static void runUpgrades(IDatabase database) throws Exception {
		Upgrades upgrades = new Upgrades();
		upgrades.run(database);
	}

	private void run(IDatabase database) throws Exception {
		IUpgrade nextUpgrade = null;
		while ((nextUpgrade = findNextUpgrade(database)) != null) {
			log.trace("execute update from v({}) to v{}", nextUpgrade.getInitialVersions(),
					nextUpgrade.getEndVersion());
			nextUpgrade.exec(database);
			updateVersion(nextUpgrade.getEndVersion(), database);
		}
		log.trace("no more upgrades");
	}

	private void updateVersion(int version, IDatabase database) throws Exception {
		NativeSql.on(database).runUpdate("update openlca_version set version = " + version);
	}

	private IUpgrade findNextUpgrade(IDatabase database) {
		int version = database.getVersion();
		for (IUpgrade upgrade : upgrades) {
			for (int upgradeVersion : upgrade.getInitialVersions()) {
				if (upgradeVersion == version)
					return upgrade;
			}
		}
		return null;
	}
}
