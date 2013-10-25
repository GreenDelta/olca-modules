package org.openlca.core;

import java.io.File;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.core.math.BlasMatrixFactory;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.math.JavaMatrixFactory;
import org.openlca.jblas.Library;

public class TestSession {

	private static IDatabase mysqlDatabase;
	private static IDatabase derbyDatabase;
	private static IMatrixFactory matrixFactory;

	public static IDatabase getDefaultDatabase() {
		return getDerbyDatabase();
	}

	public static IMatrixFactory getMatrixFactory() {
		if (matrixFactory != null)
			return matrixFactory;
		if (!Library.isLoaded()) {
			String tmpDirPath = System.getProperty("java.io.tmpdir");
			File tempDir = new File(tmpDirPath);
			Library.loadFromDir(tempDir);
		}
		if (Library.isLoaded())
			matrixFactory = new BlasMatrixFactory();
		else
			matrixFactory = new JavaMatrixFactory();
		return matrixFactory;
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
