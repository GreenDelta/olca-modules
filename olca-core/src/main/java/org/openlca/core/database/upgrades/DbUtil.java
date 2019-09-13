package org.openlca.core.database.upgrades;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DbUtil {

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
	String getTextType() {
		switch (dbType) {
		case TYPE_DERBY:
			return "CLOB(64 K)";
		case TYPE_MYSQL:
			return "TEXT";
		default:
			return "CLOB(64 K)";
		}
	}

	String getBlobType() {
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
	void dropTable(String table) {
		if (!tableExists(table)) {
			log.trace("Table {} does not exist", table);
			return;
		}
		log.info("drop table {}", table);
		try {
			NativeSql.on(database).runUpdate("DROP TABLE " + table);
		} catch (Exception e) {
			throw new RuntimeException("failed to drop table: " + table, e);
		}
	}

	/**
	 * Checks if a table with the given name exists in the database. If not it is
	 * created using the given table definition.
	 */
	void createTable(String table, String tableDef) {
		if (tableExists(table)) {
			log.trace("table exists");
			return;
		}
		log.info("create table {}", table);
		try {
			NativeSql.on(database).runUpdate(tableDef);
		} catch (Exception e) {
			throw new RuntimeException("failed to create table: " + table, e);
		}
	}

	/**
	 * Returns true if a table with the given name exits.
	 */
	boolean tableExists(String table) {
		try (Connection con = database.createConnection()) {
			DatabaseMetaData meta = con.getMetaData();
			try (ResultSet rs = meta.getTables(null, null, "%", null)) {
				while (rs.next()) {
					String other = rs.getString(3);
					if (table.equalsIgnoreCase(other))
						return true;
				}
				return false;
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to check if table exists: " + table, e);
		}
	}

	/**
	 * Create a column with the given definition in the given table. The column
	 * definition must contain the name and type of the column, like `formula
	 * VARCHAR(1000)`. If the column already exists in the table (also with another
	 * data type), nothing is done and `false` is returned. Otherwise the column is
	 * added and `true` is returned.
	 */
	boolean createColumn(String table, String definition) {
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
	boolean dropColumn(String table, String column) {
		if (!columnExists(table, column))
			return false;
		log.info("drop column {} in table {}", column, table);
		String stmt = "ALTER TABLE " + table + " DROP COLUMN " + column;
		try {
			NativeSql.on(database).runUpdate(stmt);
			return true;
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to drop column " + table + "." + column, e);
		}
	}

	/**
	 * Returns true if a column with the given name exists in the table with the
	 * given name.
	 */
	boolean columnExists(String table, String column) {
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
	void renameColumn(String table, String column, String definition) {

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

	static void setVersion(IDatabase db, int v) {
		try {
			NativeSql.on(db).runUpdate(
					"UPDATE openlca_version SET version = " + v);
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to set database version to " + v, e);
		}
	}

}
