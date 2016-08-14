package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;

/** Upgrades the database to version 5. */
class Upgrade5 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 4 };
	}

	@Override
	public int getEndVersion() {
		return 5;
	}

	@Override
	public void exec(IDatabase db) throws Exception {
		Util util = new Util(db);
		util.renameColumn("tbl_sources", "doi", "url", "VARCHAR(255)");
		util.renameColumn("tbl_process_links", "f_recipient",
				"f_process", "BIGINT");
		util.createColumn("tbl_process_links", "f_exchange",
				"f_exchange BIGINT");
		// TODO: fill exchange IDs
	}

}
