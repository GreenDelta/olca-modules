package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

public class Upgrade15 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[]{14};
	}

	@Override
	public int getEndVersion() {
		return 15;
	}

	@Override
	public void exec(IDatabase db) {
		var u = new DbUtil(db);
		u.createColumn("tbl_exchanges", "default_provider_type SMALLINT default 0");
	}
}
