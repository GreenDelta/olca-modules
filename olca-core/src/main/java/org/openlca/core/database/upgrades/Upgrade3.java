package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public class Upgrade3 implements IUpgrade {

	private UpgradeUtil util;

	@Override
	public int[] getInitialVersions() {
		return new int[] { 4 };
	}

	@Override
	public int getEndVersion() {
		return 5;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		this.util = new UpgradeUtil(database);
		createDQSystemTable();
		createDQIndicatorTable();
		createDQScoreTable();
	}

	private void createDQSystemTable() throws Exception {
		util.checkCreateTable("tbl_dq_systems",
				"CREATE TABLE tbl_dq_systems ( " + "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), " + "ref_id VARCHAR(36), "
						+ "version BIGINT, " + "last_change BIGINT, "
						+ "f_category BIGINT, " + "description CLOB(64 K), "
						+ "has_uncertainties SMALLINT default 0, "
						+ "PRIMARY KEY (id)) ");
	}

	private void createDQIndicatorTable() throws Exception {
		util.checkCreateTable("tbl_dq_indicators",
				"CREATE TABLE tbl_dq_indicators ( " + "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), " + "position INTEGER NOT NULL, "
						+ "f_dq_system BIGINT, "
						+ "PRIMARY KEY (id)) ");
	}

	private void createDQScoreTable() throws Exception {
		util.checkCreateTable("tbl_dq_scores", "CREATE TABLE tbl_dq_scores ( "
				+ "id BIGINT NOT NULL, " + "position INTEGER NOT NULL, "
				+ "description CLOB(64 K), " + "uncertainty DOUBLE default 0, "
				+ "f_dq_indicator BIGINT, " + "PRIMARY KEY (id)) ");
	}

}
