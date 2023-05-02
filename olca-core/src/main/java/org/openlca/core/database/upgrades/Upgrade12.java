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
