package org.openlca.core.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DbUtils {

	private DbUtils() {
	}

	/**
	 * Returns the version of the given database, or -1 if an error occured.
	 */
	static int getVersion(IDatabase database) {
		try {
			final int[] version = new int[1];
			NativeSql.on(database).query("select version from openlca_version",
					result -> {
						version[0] = result.getInt(1);
						return true;
					});
			return version[0];
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(DbUtils.class);
			log.error("failed to get the database version", e);
			return -1;
		}
	}
}
