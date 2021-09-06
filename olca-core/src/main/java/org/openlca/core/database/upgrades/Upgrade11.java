package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public class Upgrade11 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{10};
	}

	@Override
	public int getEndVersion() {
		return 11;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		u.createTable(
			"tbl_results",
			"CREATE TABLE tbl_results (" +
				"    id                 BIGINT NOT NULL," +
				"    ref_id             VARCHAR(36)," +
				"    name               VARCHAR(2048)," +
				"    version            BIGINT," +
				"    last_change        BIGINT," +
				"    f_category         BIGINT," +
				"    tags               VARCHAR(255)," +
				"    library            VARCHAR(255)," +
				"    description        CLOB(64 K)," +
				"    PRIMARY KEY (id)" +
				")");

		u.createTable(
			"tbl_result_flows",
			"CREATE TABLE tbl_result_flows (" +
				"    id                        BIGINT NOT NULL," +
				"    f_result                  BIGINT," +
				"    f_flow                    BIGINT," +
				"    f_unit                    BIGINT," +
				"    is_input                  SMALLINT default 0," +
				"    f_flow_property_factor    BIGINT," +
				"    resulting_amount_value    DOUBLE," +
				"    f_location                BIGINT," +
				"    description               CLOB(64 K)," +
				"    PRIMARY KEY (id)" +
				")"
		);

		u.createTable(
			"tbl_result_impacts",
			"CREATE TABLE tbl_result_impacts (" +
				"    id                 BIGINT NOT NULL," +
				"    f_result           BIGINT," +
				"    f_impact_category  BIGINT," +
				"    amount             DOUBLE," +
				"    description        CLOB(64 K)," +
				"    PRIMARY KEY (id)" + ")"
		);

	}
}

