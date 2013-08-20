package org.openlca.io;

import java.io.File;

import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

public class TestSession {

	private static IDatabase derbyDatabase;

	public static IDatabase getDerbyDatabase() {
		if (derbyDatabase == null) {
			String tmpDirPath = System.getProperty("java.io.tmpdir");
			String dbName = "olca_test_db_1.4";
			File tmpDir = new File(tmpDirPath);
			File folder = new File(tmpDir, dbName);
			boolean newDb = folder.exists();
			DerbyDatabase db = new DerbyDatabase(folder);
			if (newDb)
				db.fill(DatabaseContent.ALL_REF_DATA);
			derbyDatabase = db;
		}
		return derbyDatabase;
	}
}
