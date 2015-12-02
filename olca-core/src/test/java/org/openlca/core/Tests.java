package org.openlca.core;

import java.io.File;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.upgrades.Upgrades;
import org.openlca.core.math.IMatrixSolver;
import org.openlca.core.math.JavaSolver;

public class Tests {

	private static IDatabase db;

	public static IMatrixSolver getDefaultSolver() {
		return new JavaSolver();
	}

	public static void emptyCache() {
		if (db != null) {
			db.getEntityFactory().getCache().evictAll();
		}
	}

	public static IDatabase getDb() {
		if (db == null) {
			String tmpDirPath = System.getProperty("java.io.tmpdir");
			String dbName = "olca_test_db_1.4";
			File tmpDir = new File(tmpDirPath);
			File folder = new File(tmpDir, dbName);
			db = new DerbyDatabase(folder);
			try {
				// (currently) it should be always possible to run the database
				// updates on databases that were already updated as the
				// updated should check if an update is necessary or not. Thus
				// we reset the version here and test if the updates work.
				String versionReset = "update openlca_version set version = 1";
				NativeSql.on(db).runUpdate(versionReset);
				Upgrades.runUpgrades(db);
			} catch (Exception e) {
				throw new RuntimeException("DB-upgrades failed", e);
			}
		}
		return db;
	}


}
