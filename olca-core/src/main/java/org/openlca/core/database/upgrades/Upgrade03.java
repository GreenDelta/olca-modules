package org.openlca.core.database.upgrades;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.Derby;

class Upgrade03 implements IUpgrade {

	private DbUtil util;
	private IDatabase database;

	@Override
	public int[] getInitialVersions() {
		return new int[] { 1, 2 };
	}

	@Override
	public int getEndVersion() {
		return 3;
	}

	@Override
	public void exec(IDatabase database) {
		this.database = database;
		this.util = new DbUtil(database);
		createNwSetTable();
		createNwFactorTable();
		changeSimpleColumns();
		updateParameterRedefs();
		updateMappingTable();
		addVersionColumns();
	}

	private void changeSimpleColumns() {
		util.createColumn("tbl_sources", "external_file VARCHAR(255)");
		util.createColumn("tbl_parameters", "external_source VARCHAR(255)");
		util.createColumn("tbl_parameters", "source_type VARCHAR(255)");
		util.createColumn("tbl_impact_factors", "formula VARCHAR(1000)");
		util.createColumn("tbl_processes", "kmz " + util.getBlobType());
		util.createColumn("tbl_locations", "kmz " + util.getBlobType());
		util.dropColumn("tbl_process_docs", "last_change");
		util.dropColumn("tbl_process_docs", "version");
	}

	private void updateMappingTable() {
		util.dropTable("tbl_mappings");
		String tableDef;
		if (database instanceof Derby) {
			tableDef = "CREATE TABLE tbl_mapping_files ("
					+ "id BIGINT NOT NULL, "
					+ "file_name VARCHAR(255), "
					+ "content BLOB(16 M), "
					+ "PRIMARY KEY (id))";
		} else {
			tableDef = "CREATE TABLE tbl_mapping_files ("
					+ "id BIGINT NOT NULL, "
					+ "file_name VARCHAR(255), "
					+ "content MEDIUMBLOB, "
					+ "PRIMARY KEY (id))";
		}
		util.createTable("tbl_mapping_files", tableDef);
	}

	private void createNwSetTable() {
		String tableDef = "CREATE TABLE tbl_nw_sets ("
				+ "id BIGINT NOT NULL, "
				+ "ref_id VARCHAR(36), "
				+ "description " + util.getTextType() + ", "
				+ "name VARCHAR(255), "
				+ "reference_system VARCHAR(255), "
				+ "f_impact_method BIGINT,  "
				+ "weighted_score_unit VARCHAR(255), "
				+ "PRIMARY KEY (id))";
		util.createTable("tbl_nw_sets", tableDef);
		copyNwSetTable();
		util.dropTable("tbl_normalisation_weighting_sets");
	}

	private void copyNwSetTable() {
		if (!util.tableExists("tbl_normalisation_weighting_sets"))
			return;
		String query = "select * from tbl_normalisation_weighting_sets";
		try {
			NativeSql.on(database).query(query, (result) -> {
				copyNwSet(result);
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to copy table: tbl_normalisation_weighting_sets", e);
		}
	}

	private void copyNwSet(ResultSet result) {
		String stmt = "insert into tbl_nw_sets (id, ref_id, name, "
				+ "f_impact_method, weighted_score_unit) values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, 1,
				(int i, PreparedStatement ps) -> {
					ps.setLong(1, result.getLong("id"));
					ps.setString(2, UUID.randomUUID().toString());
					ps.setString(3, result.getString("reference_system"));
					ps.setLong(4, result.getLong("f_impact_method"));
					ps.setString(5, result.getString("unit"));
					return true;
				});
	}

	private void createNwFactorTable() {
		String tableDef = "CREATE TABLE tbl_nw_factors ("
				+ " id BIGINT NOT NULL," + " weighting_factor DOUBLE,"
				+ " normalisation_factor DOUBLE,"
				+ " f_impact_category BIGINT," + " f_nw_set BIGINT,"
				+ " PRIMARY KEY (id))";
		util.createTable("tbl_nw_factors", tableDef);
		copyNwFactorTable();
		util.dropTable("tbl_normalisation_weighting_factors");
	}

	private void copyNwFactorTable() {
		if (!util.tableExists("tbl_normalisation_weighting_factors"))
			return;
		String query = "select * from tbl_normalisation_weighting_factors";
		try {
			NativeSql.on(database).query(query, (result) -> {
				copyNwFactor(result);
				return true;
			});
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to copy table: tbl_normalisation_weighting_factors", e);
		}
	}

	private void copyNwFactor(ResultSet result) {
		String stmt = "insert into tbl_nw_factors(id, weighting_factor, "
				+ "normalisation_factor, f_impact_category, f_nw_set) "
				+ "values (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, 1,
				(int i, PreparedStatement ps) -> {
					prepareFactorRecord(result, ps);
					return true;
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

	/**
	 * Changes in parameter redefinitions: It is now allowed to redefine process and
	 * LCIA method parameters.
	 */
	private void updateParameterRedefs() {
		util.renameColumn("tbl_parameter_redefs",
				"f_process", "f_context BIGINT");
		if (util.columnExists("tbl_parameter_redefs", "context_type"))
			return;
		util.createColumn("tbl_parameter_redefs", "context_type VARCHAR(255)");
		try {
			NativeSql.on(database).runUpdate(
					"update tbl_parameter_redefs "
							+ "set context_type = 'PROCESS' "
							+ "where f_context is not null");
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to update tbl_parameter_redefs", e);
		}
	}

	private void addVersionColumns() {
		String[] tables = { "tbl_actors", "tbl_sources", "tbl_unit_groups",
				"tbl_flow_properties", "tbl_flows", "tbl_processes",
				"tbl_product_systems", "tbl_impact_methods", "tbl_projects" };
		for (String table : tables) {
			util.createColumn(table, "version BIGINT");
			util.createColumn(table, "last_change BIGINT");
		}
	}

}
