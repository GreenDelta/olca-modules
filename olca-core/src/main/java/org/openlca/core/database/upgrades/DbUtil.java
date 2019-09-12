package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtil {

	private final int TYPE_DERBY = 0;
	private final int TYPE_MYSQL = 1;

	private final int dbType;
	private IDatabase database;

	private Logger log = LoggerFactory.getLogger(getClass());

	public DbUtil(IDatabase database) {
		this.database = database;
		if (database instanceof MySQLDatabase)
			dbType = TYPE_MYSQL;
		else
			dbType = TYPE_DERBY;
	}

	/** Get the database type for storing long text values. */
	public String getTextType() {
		switch (dbType) {
		case TYPE_DERBY:
			return "CLOB(64 K)";
		case TYPE_MYSQL:
			return "TEXT";
		default:
			return "CLOB(64 K)";
		}
	}

	public String getBlobType() {
		switch (dbType) {
		case TYPE_DERBY:
			return "BLOB(16 M)";
		case TYPE_MYSQL:
			return "MEDIUMBLOB";
		default:
			return "BLOB(16 M)";
		}
	}

	/**
	 * Deletes the table with the given name from the database if it exists.
	 */
	public void dropTable(String tableName) throws Exception {
		log.trace("Try to drop table {}", tableName);
		if (!tableExists(tableName)) {
			log.trace("Table {} does not exist", tableName);
		} else {
			NativeSql.on(database).runUpdate("DROP TABLE " + tableName);
		}
	}

	/**
	 * Checks if a table with the given name exists in the database. If not it is
	 * created using the given table definition.
	 */
	public void createTable(String table, String tableDef) throws Exception {
		log.trace("Check if table {} exists", table);
		if (tableExists(table))
			log.trace("table exists");
		else {
			log.info("create table {}", table);
			NativeSql.on(database).runUpdate(tableDef);
		}
	}

	/**
	 * Returns true if a table with the given name exits.
	 */
	public boolean tableExists(String tableName) throws Exception {
		try (Connection con = database.createConnection()) {
			DatabaseMetaData metaData = con.getMetaData();
			try (ResultSet rs = metaData.getTables(null, null, "%", null)) {
				while (rs.next()) {
					String otherName = rs.getString(3);
					if (tableName.equalsIgnoreCase(otherName))
						return true;
				}
				return false;
			}
		}
	}

	/**
	 * Create a column with the given definition in the given table. The column
	 * definition must contain the name and type of the column, like `formula
	 * VARCHAR(1000)`. If the column already exists in the table (also with another
	 * data type), nothing is done and `false` is returned. Otherwise the column is
	 * added and `true` is returned.
	 */
	public boolean createColumn(String table, String definition) {
		if (table == null || definition == null)
			return false;

		String column;
		try {
			column = definition.split(" ")[0].trim();
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"invalid column definition " + definition);
		}

		if (columnExists(table, column)) {
			log.debug("column {}.{} already exists", table, column);
			return false;
		}

		try {
		log.info("add column {} to {}", column, table);
			String stmt = "ALTER TABLE " + table + " ADD COLUMN " + definition;
		NativeSql.on(database).runUpdate(stmt);
		return true;
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to add column " + table + "." + column, e);
		}
	}

	/** Deletes the given column from the given table if it exists. */
	public boolean dropColumn(String table, String column) throws Exception {
		log.trace("drop column {} in table {}", column, table);
		if (!columnExists(table, column))
			return false;
		String stmt = "ALTER TABLE " + table + " DROP COLUMN " + column;
		NativeSql.on(database).runUpdate(stmt);
		return true;
	}

	/**
	 * Returns true if a column with the given name exists in the table with the
	 * given name.
	 */
	public boolean columnExists(String table, String column) {
		try (Connection con = database.createConnection()) {
			DatabaseMetaData metaData = con.getMetaData();
			try (ResultSet rs = metaData.getColumns(null, null, "%", "%")) {
				while (rs.next()) {
					String t = rs.getString(3);
					String c = rs.getString(4);
					if (t.equalsIgnoreCase(table) && c.equalsIgnoreCase(column))
						return true;
				}
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to search for column " + table + "." + column, e);
		}
	}

	/**
	 * Rename the given column in the given table. A full definition of the new
	 * column (name + data type, e.g. `formula VARCHAR(1000)`) must be given. If the
	 * old column does not exist the new column is created if necessary.
	 */
	public void renameColumn(String table, String column, String definition) {

		String newCol;
		try {
			newCol = definition.split(" ")[0];
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"invalid column definition " + definition);
		}

		if (columnExists(table, newCol)) {
			log.debug("column {}.{} already exists", table, newCol);
			return;
		}
		if (!columnExists(table, column)) {
			log.warn("column {}.{} does not exists for renaming", table, column);
			createColumn(table, definition);
			return;
		}

		log.info("rename column {}.{} to {}.{}", table, column, table, newCol);
		String query = database instanceof DerbyDatabase
				? "RENAME COLUMN " + table + "." + column + " TO " + newCol
				: "ALTER TABLE " + table + " CHANGE " + column
						+ " " + definition;
		try {
			NativeSql.on(database).runUpdate(query);
		} catch (Exception e) {
			throw new RuntimeException("failed to rename column: " + query, e);
		}
	}

	public void setVersion(int v) throws SQLException {
		NativeSql.on(database).runUpdate(
				"UPDATE openlca_version SET version = " + v);
	}

}
