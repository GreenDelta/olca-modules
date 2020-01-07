package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

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
		u.createColumn("tbl_impact_categories", "f_category BIGINT");

		if (u.tableExists("tbl_impact_links"))
			return;
		u.createTable("tbl_impact_links",
				"CREATE TABLE tbl_impact_links (" +
						" f_impact_method    BIGINT," +
						" f_impact_category  BIGINT)");
		try {
			NativeSql.on(db).runUpdate(
					"INSERT INTO tbl_impact_links "
							+ " (f_impact_method, f_impact_category) "
							+ " select f_impact_method, id "
							+ " from tbl_impact_categories");
		} catch (Exception e) {
			throw new RuntimeException("failed to copy impact links", e);
		}
		// TODO: parameters + regionalization

		u.createColumn("tbl_impact_categories", "parameter_mean VARCHAR(255)");
		// TODO: copy parameters to each LCIA category from the method

	}
}
