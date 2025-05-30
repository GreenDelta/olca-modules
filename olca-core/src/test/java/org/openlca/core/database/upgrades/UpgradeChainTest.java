package org.openlca.core.database.upgrades;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.util.Dirs;

public class UpgradeChainTest {

	private Derby db;

	@Before
	public void setup() throws Exception {
		db = new Derby(
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

		// roll back Upgrade15
		u.dropColumn("tbl_exchanges", "default_provider_type");

		// roll back Upgrade14
		u.dropTable("tbl_analysis_groups");
		u.dropTable("tbl_analysis_group_processes");
		u.dropColumn("tbl_parameters", "formula");
		u.createColumn("tbl_parameters","formula VARCHAR(1000)");
		for (var table : List.of(
				"tbl_exchanges",
				"tbl_impact_factors",
				"tbl_parameters",
				"tbl_parameter_redefs")) {
			u.createColumn(table, "parameter1_formula VARCHAR(1000)");
			u.createColumn(table, "parameter2_formula VARCHAR(1000)");
			u.createColumn(table, "parameter3_formula VARCHAR(1000)");
		}

		// roll back Upgrade12
		for (var table : catEntityTables) {
			u.dropColumn(table, "other_properties");
		}
		String[][] renamed12 = {
				{"data_completeness", "completeness CLOB(64 K)"},
				{"sampling_procedure", "sampling CLOB(64 K)"},
				{"f_data_owner", "f_dataset_owner BIGINT"},
				{"access_restrictions", "restrictions CLOB(64 K)"}
		};
		for (var r : renamed12) {
			u.renameColumn("tbl_process_docs", r[0], r[1]);
		}
		u.dropColumn("tbl_process_docs", "use_advice");
		u.dropColumn("tbl_process_docs", "flow_completeness");
		u.dropTable("tbl_compliance_declarations");
		u.dropTable("tbl_reviews");

		// roll back Upgrade11
		u.dropTable("tbl_epds");
		u.dropTable("tbl_epd_modules");
		u.dropTable("tbl_results");
		u.dropTable("tbl_flow_results");
		u.dropTable("tbl_impact_results");
		u.dropColumn("tbl_parameter_redefs", "is_protected");
		u.dropColumn("tbl_impact_methods", "f_source");
		u.dropColumn("tbl_impact_methods", "code");
		u.dropColumn("tbl_impact_categories", "f_source");
		u.dropColumn("tbl_impact_categories", "code");
		u.dropColumn("tbl_impact_categories", "direction");
		u.dropColumn("tbl_process_links", "provider_type");

		// roll back Upgrade9 & Upgrade10
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
		u.dropColumn("tbl_project_variants", "description");
		u.dropColumn("tbl_projects", "is_with_costs");
		u.dropColumn("tbl_projects", "is_with_regionalization");
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

		// check Upgrade9 & Upgrade10
		assertTrue(u.tableExists("tbl_parameter_redef_sets"));
		assertTrue(u.columnExists("tbl_parameter_redefs", "description"));
		assertTrue(u.tableExists("tbl_impact_links"));
		assertTrue(u.columnExists("tbl_impact_categories", "f_category"));
		assertTrue(u.columnExists("tbl_exchanges", "f_location"));
		assertTrue(u.columnExists("tbl_impact_factors", "f_location"));
		assertTrue(u.columnExists("tbl_locations", "geodata"));
		assertTrue(u.columnExists("tbl_allocation_factors", "formula"));
		assertTrue(u.tableExists("tbl_libraries"));
		assertTrue(u.columnExists("tbl_project_variants", "description"));
		assertTrue(u.columnExists("tbl_projects", "is_with_costs"));
		assertTrue(u.columnExists("tbl_projects", "is_with_regionalization"));
		for (var table : catEntityTables) {
			assertTrue(u.columnExists(table, "tags"));
			assertTrue(u.columnExists(table, "library"));
		}

		// check Upgrade11
		assertTrue(u.tableExists("tbl_epds"));
		assertTrue(u.tableExists("tbl_epd_modules"));
		assertTrue(u.tableExists("tbl_results"));
		assertTrue(u.tableExists("tbl_flow_results"));
		assertTrue(u.tableExists("tbl_impact_results"));
		assertTrue(u.columnExists("tbl_parameter_redefs", "is_protected"));
		assertTrue(u.columnExists("tbl_impact_methods", "f_source"));
		assertTrue(u.columnExists("tbl_impact_methods", "code"));
		assertTrue(u.columnExists("tbl_impact_categories", "f_source"));
		assertTrue(u.columnExists("tbl_impact_categories", "code"));
		assertTrue(u.columnExists("tbl_impact_categories", "direction"));
		assertTrue(u.columnExists("tbl_process_links", "provider_type"));

		// check Upgrade12
		for (var table : catEntityTables) {
			assertTrue(
					"column other_properties missing in table " + table,
					u.columnExists(table, "other_properties"));
		}
		for (var r : renamed12) {
			assertTrue(u.columnExists("tbl_process_docs", r[0]));
		}
		assertTrue(u.columnExists("tbl_process_docs", "use_advice"));
		assertTrue(u.columnExists("tbl_process_docs", "flow_completeness"));
		assertTrue(u.tableExists("tbl_reviews"));
		assertTrue(u.tableExists("tbl_compliance_declarations"));

		// check Upgrade13
		assertTrue(u.columnExists("tbl_epds", "epd_type"));
		assertTrue(u.columnExists("tbl_epds", "valid_from"));
		assertTrue(u.columnExists("tbl_epds", "valid_until"));
		assertTrue(u.columnExists("tbl_epds", "f_location"));
		assertTrue(u.columnExists("tbl_epds", "f_original_epd"));
		assertTrue(u.columnExists("tbl_epds", "manufacturing"));
		assertTrue(u.columnExists("tbl_epds", "product_usage"));
		assertTrue(u.columnExists("tbl_epds", "use_advice"));
		assertTrue(u.columnExists("tbl_epds", "registration_id"));
		assertTrue(u.columnExists("tbl_epds", "f_data_generator"));

		// check Upgrade14
		assertTrue(u.tableExists("tbl_analysis_groups"));
		assertTrue(u.tableExists("tbl_analysis_group_processes"));
		var paramFormulaSize = new AtomicReference<String>();
		NativeSql.on(db).query("select c.columndatatype from sys.syscolumns c" +
				" join sys.systables t on c.referenceid = t.tableid" +
				" where t.tablename = 'TBL_PARAMETERS'" +
				"  and c.columnname = 'FORMULA'", r -> {
			paramFormulaSize.set(r.getString(1));
			return false;
		});
		assertEquals("VARCHAR(5120)", paramFormulaSize.get());

		// check Upgrade15
		assertTrue(u.columnExists("tbl_exchanges", "default_provider_type"));

		// finally, check that we now have the current database version
		assertEquals(IDatabase.CURRENT_VERSION, db.getVersion());
	}

}
