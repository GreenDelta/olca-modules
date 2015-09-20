package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public class Upgrade3 implements IUpgrade {

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
		UpgradeUtil util = new UpgradeUtil(database);
		util.checkCreateColumn("tbl_locations", "f_category", "f_category BIGINT");
		util.checkCreateColumn("tbl_parameters", "f_category", "f_category BIGINT");
	}

}
