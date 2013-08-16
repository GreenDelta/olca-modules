package org.openlca.core;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.jblas.Library;

public class TestSession {

	private static IDatabase mysqlDatabase;
	private static IDatabase derbyDatabase;

	public static IDatabase getDefaultDatabase() {
		return getDerbyDatabase();
	}

	/** Tries to load the native BLAS library if it is not yet done. */
	public static void prefereBlas() {
		if (Library.isLoaded())
			return;
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		File tempDir = new File(tmpDirPath);
		Library.loadFromDir(tempDir);
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
		}
		return derbyDatabase;
	}

}
