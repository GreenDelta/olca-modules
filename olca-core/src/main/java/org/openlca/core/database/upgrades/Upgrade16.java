package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

class Upgrade16 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 15 };
	}

	@Override
	public int getEndVersion() {
		return 16;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);
		u.createTable("tbl_data_packages", """
				CREATE TABLE tbl_data_packages (

					name       VARCHAR(255),
					version    VARCHAR(255),
					url        VARCHAR(1000),
					is_library SMALLINT default 0,

				    PRIMARY KEY (name)
				)
				""");
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
				"tbl_results",
				"tbl_epds",
				"tbl_social_indicators",
				"tbl_sources",
				"tbl_unit_groups",
		};
		for (var table : tables) {
			u.renameColumn(table, "library", "data_package VARCHAR(255)");
		}
		NativeSql.on(db).query("SELECT id FROM tbl_libraries", rs -> {
			var name = rs.getString(1);
			NativeSql.on(db).runUpdate("INSERT INTO tbl_data_packages(name, is_library) VALUES ('" + name + "', 1)");
			return true;
		});
		u.dropTable("tbl_libraries");
	}
	
}
