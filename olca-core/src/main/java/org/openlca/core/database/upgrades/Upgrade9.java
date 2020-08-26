package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

class Upgrade9 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 8 };
	}

	@Override
	public int getEndVersion() {
		return 9;
	}

	@Override
	public void exec(IDatabase db) {
		DbUtil u = new DbUtil(db);
		// add tags and library fields
		String[] tables = {
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
				"tbl_unit_groups"
		};
		for (String table : tables) {
			u.createColumn(table, "tags VARCHAR(255)");
		}
	}
}
