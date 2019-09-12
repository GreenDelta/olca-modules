package org.openlca.core.database.upgrades;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.util.Dirs;

public class UpgradeChainTest {

	private DerbyDatabase db;

	@Before
	public void setup() throws Exception {
		db = new DerbyDatabase(
				Files.createTempDirectory("_olca_tests_").toFile());
	}

	@After
	public void tearDown() throws Exception {
		db.close();
		Dirs.delete(db.getDatabaseDirectory().getAbsolutePath());
	}

	@Test
	public void test() throws Exception {

		DbUtil u = new DbUtil(db);
		u.setVersion(1);

		// these rollbacks are not complete; we just want
		// to see that each upgrade was executed; also note
		// that the rollbacks are done in reverse order

		// roll back Upgrade4
		u.dropColumn("tbl_exchanges", "f_currency");
		u.dropColumn("tbl_exchanges", "cost_value");
		u.dropTable("tbl_social_indicators");
		u.renameColumn("tbl_categories", "f_category",
				"f_parent_category");

		// roll back Upgrade3
		u.dropColumn("tbl_sources", "external_file");
		u.dropTable("tbl_nw_sets");
		u.dropTable("tbl_mapping_files");
		u.dropColumn("tbl_actors", "version");

		Upgrades.runUpgrades(db);

		// check Upgrade3
		assertTrue(u.tableExists("tbl_nw_sets"));
		assertTrue(u.tableExists("tbl_mapping_files"));
		assertTrue(u.columnExists("tbl_actors", "version"));
		assertTrue(u.columnExists("tbl_sources", "external_file"));

		// check Upgrade4
		assertTrue(u.tableExists("tbl_social_indicators"));
		assertTrue(u.columnExists("tbl_exchanges", "f_currency"));
		assertTrue(u.columnExists("tbl_exchanges", "cost_value"));
		assertTrue(u.columnExists("tbl_categories", "f_category"));
	}

}
