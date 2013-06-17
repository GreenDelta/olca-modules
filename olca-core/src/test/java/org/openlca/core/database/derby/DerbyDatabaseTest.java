package org.openlca.core.database.derby;

import java.io.File;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.database.ActorDao;

public class DerbyDatabaseTest {

	private static DerbyDatabase database;

	@BeforeClass
	public static void setUp() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dbName = "test_db_"
				+ UUID.randomUUID().toString().substring(0, 5);
		File tmpDir = new File(tmpDirPath);
		File folder = new File(tmpDir, dbName);
		database = new DerbyDatabase(folder);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		database.delete();
	}

	@Test
	public void testEmptyDao() throws Exception {
		ActorDao dao = new ActorDao(database.getEntityFactory());
		Assert.assertTrue(dao.getAll().isEmpty());
	}

}
