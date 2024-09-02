package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public class Upgrade13 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[12];
	}

	@Override
	public int getEndVersion() {
		return 13;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);

		u.renameColumn("tbl_epds", "urn", "registration_id VARCHAR(2048)");

		u.createColumn("tbl_epds", "valid_from DATE");
		u.createColumn("tbl_epds", "valid_until DATE");
		u.createColumn("tbl_epds", "f_location BIGINT");
		u.createColumn("tbl_epds", "f_original_epd BIGINT");
		u.createColumn("tbl_epds", "manufacturing CLOB(64 K)");
		u.createColumn("tbl_epds", "product_usage CLOB(64 K)");
		u.createColumn("tbl_epds", "use_advice CLOB(64 K)");
		u.createColumn("tbl_epds", "registration_id VARCHAR(2048)");
		u.createColumn("tbl_epds", "f_data_generator BIGINT");
	}
}
