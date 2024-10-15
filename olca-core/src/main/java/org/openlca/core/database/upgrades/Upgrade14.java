package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

class Upgrade14 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{13};
	}

	@Override
	public int getEndVersion() {
		return 14;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		u.createTable("tbl_analysis_groups", """
				CREATE TABLE tbl_analysis_groups (

				    id     BIGINT NOT NULL,
				    name   VARCHAR(2048),
				    color  VARCHAR(255),
				    f_product_system  BIGINT,

				    PRIMARY KEY (id)
				)
				""");

		u.createTable("tbl_analysis_group_processes", """
				CREATE TABLE tbl_analysis_group_processes (
				    f_analysis_group  BIGINT NOT NULL,
				    f_process         BIGINT NOT NULL,

				    PRIMARY KEY (f_analysis_group, f_process)
				)
				""");

	}
}
