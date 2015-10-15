package org.openlca.core;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.math.JavaSolver;

public class Tests {

	private static IDatabase mysqlDatabase;
	private static IDatabase derbyDatabase;

	public static IDatabase getDb() {
		return getDerbyDatabase();
	}

	public static IMatrixSolver getDefaultSolver() {
		return new JavaSolver();
	}

	public static IDatabase getMySQLDatabase() {
		if (mysqlDatabase != null)
			return mysqlDatabase;
		String url = "jdbc:mysql://localhost:3306/olca_test_db";
		String user = "root";
		String password = null;
		mysqlDatabase = new MySQLDatabase(url, user, password);
		return mysqlDatabase;
	}

	public static void emptyCache() {
		if (mysqlDatabase != null) {
			mysqlDatabase.getEntityFactory().getCache().evictAll();
		}
		if (derbyDatabase != null) {
			derbyDatabase.getEntityFactory().getCache().evictAll();
		}
	}

	public static IDatabase getDerbyDatabase() {
		if (derbyDatabase == null) {
			String tmpDirPath = System.getProperty("java.io.tmpdir");
			String dbName = "olca_test_db_1.4";
			File tmpDir = new File(tmpDirPath);
			File folder = new File(tmpDir, dbName);
			derbyDatabase = new DerbyDatabase(folder);
			try {
				// (currently) it should be always possible to run the database
				// updates on databases that were already updated as the
				// updated should check if an update is necessary or not. Thus
				// we reset the version here and test if the updates work.
				String versionReset = "update openlca_version set version = 1";
				NativeSql.on(derbyDatabase).runUpdate(versionReset);
				Upgrades.runUpgrades(derbyDatabase);
			} catch (Exception e) {
				throw new RuntimeException("DB-upgrades failed", e);
			}
		}
		return derbyDatabase;
	}

}
