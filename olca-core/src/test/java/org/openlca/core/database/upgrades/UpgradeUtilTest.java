package org.openlca.core.database.upgrades;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.TestSession;
import org.openlca.core.database.IDatabase;

public class UpgradeUtilTest {

	private IDatabase database = TestSession.getDefaultDatabase();
	private UpgradeUtil util = new UpgradeUtil(database);

	@Test
	public void testCreateTable() throws Exception {
		Assert.assertFalse(util.tableExists("upgrade_test_table"));
		String tableDef = "create table upgrade_test_table ("
				+ "id BIGINT NOT NULL, "
				+ "ref_id VARCHAR(36))";
		util.checkCreateTable("upgrade_test_table", tableDef);
		Assert.assertTrue(util.tableExists("upgrade_test_table"));
		Assert.assertTrue(util.columnExists("upgrade_test_table", "id"));
		util.dropTable("upgrade_test_table");
		Assert.assertFalse(util.tableExists("upgrade_test_table"));
	}

	@Test
	public void testAddColumn() throws Exception {
		String tableDef = "create table upgrade_test_table ("
				+ "id BIGINT NOT NULL, "
				+ "ref_id VARCHAR(36))";
		util.checkCreateTable("upgrade_test_table", tableDef);
		Assert.assertFalse(util.columnExists("upgrade_test_table", "test_col"));
		String colDef = "test_col VARCHAR(255)";
		util.checkCreateColumn("upgrade_test_table", "test_col", colDef);
		Assert.assertTrue(util.columnExists("upgrade_test_table", "test_col"));
		util.dropTable("upgrade_test_table");
		Assert.assertFalse(util.tableExists("upgrade_test_table"));
	}
}
