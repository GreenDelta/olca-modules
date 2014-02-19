package org.openlca.core.database.upgrades;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

class Upgrade1 implements IUpgrade {

	private UpgradeUtil util;
	private IDatabase database;

	@Override
	public int getInitialVersion() {
		return 1;
	}

	@Override
	public int getEndVersion() {
		return 2;
	}

	@Override
	public void exec(IDatabase database) throws Exception {
		this.database = database;
		this.util = new UpgradeUtil(database);
		createNwSetTable();
		createNwFactorTable();
		util.checkCreateColumn("tbl_sources", "external_file",
				"external_file VARCHAR(255)");
		util.checkCreateColumn("tbl_parameters", "external_source",
				"external_source VARCHAR(255)");
		util.checkCreateColumn("tbl_parameters", "source_type",
				"source_type VARCHAR(255)");
		util.renameColumn("tbl_parameter_redefs", "f_process", "f_context",
				"BIGINT");
	}

	private void createNwSetTable() throws Exception {
		String tableDef = "CREATE TABLE tbl_nw_sets (" + "id BIGINT NOT NULL, "
				+ "ref_id VARCHAR(36), " + "description " + util.getTextType()
				+ ", " + "name VARCHAR(255), "
				+ "reference_system VARCHAR(255), "
				+ "f_impact_method BIGINT,  "
				+ "weighted_score_unit VARCHAR(255), " + "PRIMARY KEY (id))";
		util.checkCreateTable("tbl_nw_sets", tableDef);
		copyNwSetTable();
		util.dropTable("tbl_normalisation_weighting_sets");
	}

	private void copyNwSetTable() throws Exception {
		if (!util.tableExists("tbl_normalisation_weighting_sets"))
			return;
		String query = "select * from tbl_normalisation_weighting_sets";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				copyNwSet(result);
				return true;
			}
		});
	}

	private void copyNwSet(final ResultSet result) throws SQLException {
		String stmt = "insert into tbl_nw_sets (id, ref_id, name, "
				+ "f_impact_method, weighted_score_unit) values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, 1,
				new NativeSql.BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement stmt)
							throws SQLException {
						stmt.setLong(1, result.getLong("id"));
						stmt.setString(2, UUID.randomUUID().toString());
						stmt.setString(3, result.getString("reference_system"));
						stmt.setLong(4, result.getLong("f_impact_method"));
						stmt.setString(5, result.getString("unit"));
						return true;
					}
				});
	}

	private void createNwFactorTable() throws Exception {
		String tableDef = "CREATE TABLE tbl_nw_factors ("
				+ " id BIGINT NOT NULL," + " weighting_factor DOUBLE,"
				+ " normalisation_factor DOUBLE,"
				+ " f_impact_category BIGINT," + " f_nw_set BIGINT,"
				+ " PRIMARY KEY (id))";
		util.checkCreateTable("tbl_nw_factors", tableDef);
		copyNwFactorTable();
		util.dropTable("tbl_normalisation_weighting_factors");
	}

	private void copyNwFactorTable() throws Exception {
		if (!util.tableExists("tbl_normalisation_weighting_factors"))
			return;
		String query = "select * from tbl_normalisation_weighting_factors";
		NativeSql.on(database).query(query, new NativeSql.QueryResultHandler() {
			@Override
			public boolean nextResult(ResultSet result) throws SQLException {
				copyNwFactor(result);
				return true;
			}
		});
	}

	private void copyNwFactor(final ResultSet result) throws SQLException {
		String stmt = "insert into tbl_nw_factors(id, weighting_factor, "
				+ "normalisation_factor, f_impact_category, f_nw_set) "
				+ "values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, 1,
				new NativeSql.BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement stmt)
							throws SQLException {
						prepareFactorRecord(result, stmt);
						return true;
					}
				});
	}

	private void prepareFactorRecord(final ResultSet result,
			PreparedStatement stmt) throws SQLException {
		stmt.setLong(1, result.getLong("id"));
		double wf = result.getDouble("weighting_factor");
		if (result.wasNull())
			stmt.setNull(2, Types.DOUBLE);
		else
			stmt.setDouble(2, wf);
		double nf = result.getDouble("normalisation_factor");
		if (result.wasNull())
			stmt.setNull(3, Types.DOUBLE);
		else
			stmt.setDouble(3, nf);
		stmt.setLong(4, result.getLong("f_impact_category"));
		stmt.setLong(5, result.getLong("f_normalisation_weighting_set"));
	}

}
