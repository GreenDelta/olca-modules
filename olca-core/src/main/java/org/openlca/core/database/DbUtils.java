package org.openlca.core.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtils {

	private DbUtils() {
	}

	/**
	 * Returns true if the given name is a valid database name. An valid name
	 * must be at least 4 characters long and can only contain only characters
	 * that are valid for identifiers. Additionally, reserved words like 'mysql'
	 * or 'test' are not allowed.
	 */
	public static boolean isValidName(String dbName) {
		if (dbName == null || dbName.length() < 4)
			return false;
		if (!isIdentifier(dbName))
			return false;
		return !dbName.equalsIgnoreCase("mysql");
	}

	private static boolean isIdentifier(String s) {
		if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for (int i = 1; i < s.length(); i++)
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		return true;
	}

	/**
	 * Returns the version of the given database, or -1 if an error occured.
	 */
	public static int getVersion(IDatabase database) {
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
