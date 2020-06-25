package org.openlca.core.database.upgrades;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

class Upgrade6 implements IUpgrade {

	@Override
	public int[] getInitialVersions() {
		return new int[] { 5 };
	}

	@Override
	public int getEndVersion() {
		return 6;
	}

	@Override
	public void exec(IDatabase db) {
		DbUtil u = new DbUtil(db);

		u.createTable("tbl_dq_systems",
				"CREATE TABLE tbl_dq_systems ( "
						+ "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), "
						+ "ref_id VARCHAR(36), "
						+ "version BIGINT, "
						+ "last_change BIGINT, "
						+ "f_category BIGINT, "
						+ "f_source BIGINT, "
						+ "description CLOB(64 K), "
						+ "has_uncertainties SMALLINT default 0, "
						+ "PRIMARY KEY (id)) ");

		u.createTable("tbl_dq_indicators",
				"CREATE TABLE tbl_dq_indicators ( "
						+ "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), "
						+ "position INTEGER NOT NULL, "
						+ "f_dq_system BIGINT, "
						+ "PRIMARY KEY (id)) ");

		u.createTable("tbl_dq_scores", "CREATE TABLE tbl_dq_scores ( "
				+ "id BIGINT NOT NULL, "
				+ "position INTEGER NOT NULL, "
				+ "description CLOB(64 K), "
				+ "label VARCHAR(255), "
				+ "uncertainty DOUBLE default 0, "
				+ "f_dq_indicator BIGINT, "
				+ "PRIMARY KEY (id)) ");

		u.createColumn("tbl_processes", "dq_entry VARCHAR(50)");
		u.createColumn("tbl_processes", "f_dq_system BIGINT");
		u.createColumn("tbl_processes", "f_exchange_dq_system BIGINT");
		u.createColumn("tbl_processes", "f_social_dq_system BIGINT");
		u.renameColumn("tbl_exchanges", "pedigree_uncertainty",
				"dq_entry VARCHAR(50)");
		u.createColumn("tbl_product_systems", "cutoff DOUBLE");

		db.getEntityFactory().getCache().evictAll();

		// update calls on the root categories make sure
		// that they get a path generated UUID => this
		// was introduced for the collaboration server
		// so that it can handle category paths more
		// easily
		CategoryDao dao = new CategoryDao(db);
		for (Category c : dao.getRootCategories()) {
			dao.update(c);
		}
		db.getEntityFactory().getCache().evictAll();
	}


}
