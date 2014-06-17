package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public class Upgrade2 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 3 };
	}

	@Override
	public int getEndVersion() {
		return 4;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		UpgradeUtil util = new UpgradeUtil(database);
		util.checkCreateColumn("tbl_locations", "location_type",
				"location_type VARCHAR(255)");
	}

}
