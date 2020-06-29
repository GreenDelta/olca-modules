package org.openlca.core.database.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.IDatabase;
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
	public void test() {

		DbUtil u = new DbUtil(db);
		DbUtil.setVersion(db, 1);

		// these rollbacks are not complete; we just want
		// to see that each upgrade was executed; also note
		// that the rollbacks are done in reverse order

		String[] catEntityTables = {
				"tbl_actors",
				"tbl_categories",
				"tbl_currencies",
				"tbl_dq_systems",
				"tbl_flows",
				"tbl_flow_properties",
				"tbl_impact_categories",
				"tbl_impact_methods",
				"tbl_locations",
				"tbl_parameters",
				"tbl_processes",
				"tbl_product_systems",
				"tbl_projects",
				"tbl_social_indicators",
				"tbl_sources",
				"tbl_unit_groups",
		};

		// roll back Upgrade9
		u.dropTable("tbl_parameter_redef_sets");
		u.dropColumn("tbl_parameter_redefs", "description");
		u.dropTable("tbl_impact_links");
		u.createColumn("tbl_impact_categories", "f_impact_method BIGINT");
		u.dropColumn("tbl_impact_categories", "f_category");
		u.dropColumn("tbl_exchanges", "f_location");
		u.dropColumn("tbl_impact_factors", "f_location");
		u.dropColumn("tbl_locations", "geodata");
		u.dropColumn("tbl_allocation_factors", "formula");
		u.dropTable("tbl_libraries");
		for (var table : catEntityTables) {
			u.dropColumn(table, "tags");
			u.dropColumn(table, "library");
		}

		// roll back Upgrade8
		u.dropColumn("tbl_process_links", "is_system_link");
		u.dropColumn("tbl_impact_methods", "f_author");
		u.dropColumn("tbl_impact_methods", "f_generator");
		u.dropColumn("tbl_process_docs", "preceding_dataset");
		u.dropColumn("tbl_project_variants", "is_disabled");
		u.dropTable("tbl_source_links");

		// roll back Upgrade7
		u.dropColumn("tbl_impact_methods", "parameter_mean");
		u.dropColumn("tbl_processes", "last_internal_id");
		u.dropColumn("tbl_exchanges", "internal_id");

		// roll back Upgrade6
		u.dropTable("tbl_dq_systems");
		u.dropTable("tbl_dq_indicators");
		u.dropTable("tbl_dq_scores");
		u.dropColumn("tbl_exchanges", "dq_entry");

		// roll back Upgrade5
		u.renameColumn("tbl_sources", "url", "doi VARCHAR(255)");
		u.dropColumn("tbl_process_links", "f_process");
		u.dropColumn("tbl_process_links", "f_exchange");

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

		Upgrades.on(db);

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

		// check Upgrade5
		assertTrue(u.columnExists("tbl_sources", "url"));
		assertTrue(u.columnExists("tbl_process_links", "f_process"));
		assertTrue(u.columnExists("tbl_process_links", "f_exchange"));

		// check Upgrade6
		assertTrue(u.tableExists("tbl_dq_systems"));
		assertTrue(u.tableExists("tbl_dq_indicators"));
		assertTrue(u.tableExists("tbl_dq_scores"));
		assertTrue(u.columnExists("tbl_exchanges", "dq_entry"));

		// check Upgrade7
		assertTrue(u.columnExists("tbl_impact_methods", "parameter_mean"));
		assertTrue(u.columnExists("tbl_processes", "last_internal_id"));
		assertTrue(u.columnExists("tbl_exchanges", "internal_id"));

		// check Upgrade8
		assertTrue(u.columnExists("tbl_process_links", "is_system_link"));
		assertTrue(u.columnExists("tbl_impact_methods", "f_author"));
		assertTrue(u.columnExists("tbl_impact_methods", "f_generator"));
		assertTrue(u.columnExists("tbl_process_docs", "preceding_dataset"));
		assertTrue(u.columnExists("tbl_project_variants", "is_disabled"));
		assertTrue(u.tableExists("tbl_source_links"));

		// check Upgrade9
		assertTrue(u.tableExists("tbl_parameter_redef_sets"));
		assertTrue(u.columnExists("tbl_parameter_redefs", "description"));
		assertTrue(u.tableExists("tbl_impact_links"));
		assertTrue(u.columnExists("tbl_impact_categories", "f_category"));
		assertTrue(u.columnExists("tbl_exchanges", "f_location"));
		assertTrue(u.columnExists("tbl_impact_factors", "f_location"));
		assertTrue(u.columnExists("tbl_locations", "geodata"));
		assertTrue(u.columnExists("tbl_allocation_factors", "formula"));
		assertTrue(u.tableExists("tbl_libraries"));
		for (var table : catEntityTables) {
			assertTrue(u.columnExists(table, "tags"));
			assertTrue(u.columnExists(table, "library"));
		}

		// finally, check that we now have the current database version
		assertEquals(IDatabase.CURRENT_VERSION, db.getVersion());
	}

}
