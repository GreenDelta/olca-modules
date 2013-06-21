package org.openlca.core.database.derby;

import java.io.File;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openlca.core.database.ActorDao;
import org.openlca.core.database.DatabaseContent;

public class DerbyDatabaseTest {

	private static DerbyDatabase database;

	// @BeforeClass
	public static void setUp() {
		String tmpDirPath = System.getProperty("java.io.tmpdir");
		String dbName = "test_db_"
				+ UUID.randomUUID().toString().substring(0, 5);
		File tmpDir = new File(tmpDirPath);
		File folder = new File(tmpDir, dbName);
		database = new DerbyDatabase(folder);
	}

	// @AfterClass
	public static void tearDown() throws Exception {
		database.close();
		database.delete();
	}

	@Test
	@Ignore
	public void testEmptyDao() throws Exception {
		ActorDao dao = new ActorDao(database.getEntityFactory());
		Assert.assertTrue(dao.getAll().isEmpty());
	}

	@Test
	@Ignore
	public void testAllRefData() throws Exception {
		database.fill(DatabaseContent.ALL_REF_DATA);
	}

}
