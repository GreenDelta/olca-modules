package org.openlca.core.database.upgrades;

import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;

public class Upgrade6 implements IUpgrade {

	private Util util;
	private IDatabase database;
	
	@Override
	public int[] getInitialVersions() {
		return new int[] { 5 };
	}

	@Override
	public int getEndVersion() {
		return 6;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		this.util = new Util(database);
		this.database = database;
		createDQSystemTable();
		createDQIndicatorTable();
		createDQScoreTable();
		modifyProcessTable();
		modifyExchangeTable();
		modifyProductSystemTable();
		updateCategoryRefIds();
	}

	private void createDQSystemTable() throws Exception {
		util.createTable("tbl_dq_systems",
				"CREATE TABLE tbl_dq_systems ( " + "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), " + "ref_id VARCHAR(36), "
						+ "version BIGINT, " + "last_change BIGINT, "
						+ "f_category BIGINT, " + "f_source BIGINT, "
						+ "description CLOB(64 K), " + "has_uncertainties SMALLINT default 0, "
						+ "PRIMARY KEY (id)) ");
	}

	private void createDQIndicatorTable() throws Exception {
		util.createTable("tbl_dq_indicators",
				"CREATE TABLE tbl_dq_indicators ( " + "id BIGINT NOT NULL, "
						+ "name VARCHAR(255), " + "position INTEGER NOT NULL, "
						+ "f_dq_system BIGINT, "
						+ "PRIMARY KEY (id)) ");
	}

	private void createDQScoreTable() throws Exception {
		util.createTable("tbl_dq_scores", "CREATE TABLE tbl_dq_scores ( "
				+ "id BIGINT NOT NULL, " + "position INTEGER NOT NULL, "
				+ "description CLOB(64 K), " + "label VARCHAR(255), " + "uncertainty DOUBLE default 0, "
				+ "f_dq_indicator BIGINT, " + "PRIMARY KEY (id)) ");
	}

	private void modifyProcessTable() throws Exception {
		util.createColumn("tbl_processes", "dq_entry", "dq_entry VARCHAR(50)");
		util.createColumn("tbl_processes", "f_dq_system", "f_dq_system BIGINT");
		util.createColumn("tbl_processes", "f_exchange_dq_system", "f_exchange_dq_system BIGINT");
		util.createColumn("tbl_processes", "f_social_dq_system", "f_social_dq_system BIGINT");
	}

	private void modifyExchangeTable() throws Exception {
		util.renameColumn("tbl_exchanges", "pedigree_uncertainty", "dq_entry", "VARCHAR(50)");
	}

	private void modifyProductSystemTable() throws Exception {
		util.createColumn("tbl_product_systems", "cutoff", "cutoff DOUBLE");
	}

	private void updateCategoryRefIds() {
		CategoryDao dao = new CategoryDao(database);
		List<Category> roots = dao.getRootCategories();
		for (Category category : roots)
			dao.update(category);
	}
}
