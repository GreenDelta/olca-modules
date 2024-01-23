package org.openlca.core.database.upgrades;

import java.util.List;

import org.openlca.core.database.IDatabase;

public class Upgrade12 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{11};
	}

	@Override
	public int getEndVersion() {
		return 12;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		addOtherProperties(u);
		updateProcessDocs(u);
	}

	private void updateProcessDocs(DbUtil u) {
		u.renameColumn("tbl_process_docs",
				"completeness", "data_completeness CLOB(64 K)");
		u.renameColumn("tbl_process_docs",
				"sampling", "sampling_procedure CLOB(64 K)");
		u.renameColumn("tbl_process_docs",
				"f_dataset_owner", "f_data_owner BIGINT");
		u.renameColumn("tbl_process_docs",
				"restrictions", "access_restrictions CLOB(64 K)");

		u.createColumn("tbl_process_docs", "use_advice CLOB(64 K)");

		u.createTable("tbl_compliance_declarations", """
				CREATE TABLE tbl_compliance_declarations (
				 	id        BIGINT NOT NULL,
				  f_owner   BIGINT,
				  f_system  BIGINT,
				  details   CLOB(64 K),
				  aspects   CLOB(64 K),
				  PRIMARY KEY (id)
				)
				""");

		u.createTable("tbl_reviews", """
				CREATE TABLE tbl_reviews (

				  id           BIGINT NOT NULL,
				  f_owner      BIGINT,
				  review_type  VARCHAR(255),
				  scopes       CLOB(64 K),
				  details      CLOB(64 K),
				  f_report     BIGINT,

				  PRIMARY KEY (id)
				)
				""");

		u.createTable("tbl_actor_links", """
				CREATE TABLE tbl_actor_links (
				    f_owner  BIGINT,
				    f_actor  BIGINT
				)
				""");

		// TODO: #model-doc copy review data from processes

	}

	private void addOtherProperties(DbUtil u) {
		var tables = List.of(
				"tbl_actors",
				"tbl_categories",
				"tbl_currencies",
				"tbl_dq_systems",
				"tbl_epds",
				"tbl_flow_properties",
				"tbl_flows",
				"tbl_impact_categories",
				"tbl_impact_methods",
				"tbl_locations",
				"tbl_parameters",
				"tbl_processes",
				"tbl_product_systems",
				"tbl_projects",
				"tbl_results",
				"tbl_social_indicators",
				"tbl_sources",
				"tbl_unit_groups");
		for (var table : tables) {
			u.createColumn(table, "other_properties BLOB(5 M)");
		}
	}
}
