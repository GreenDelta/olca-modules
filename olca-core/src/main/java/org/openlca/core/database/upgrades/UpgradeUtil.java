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

class UpgradeUtil {

	private final int TYPE_DERBY = 0;
	private final int TYPE_MYSQL = 1;

	private final int dbType;
	private IDatabase database;

	private Logger log = LoggerFactory.getLogger(getClass());

	UpgradeUtil(IDatabase database) {
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

	/**
	 * Deletes the table with the given name from the database if it exists.
	 */
	void dropTable(String tableName) throws Exception {
		log.trace("Try to drop table {}", tableName);
		if (!tableExists(tableName)) {
			log.trace("Table {} does not exist", tableName);
		} else {
			NativeSql.on(database).runUpdate("DROP TABLE " + tableName);
		}
	}

	/**
	 * Checks if a table with the given name exists in the database. If not it
	 * is created using the given table definition.
	 */
	void checkCreateTable(String tableName, String tableDef) throws Exception {
		log.trace("Check if table {} exists", tableName);
		if (tableExists(tableName))
			log.trace("table exists");
		else {
			log.info("create table {}", tableName);
			NativeSql.on(database).runUpdate(tableDef);
		}
	}

	/**
	 * Returns true if a table with the given name exits.
	 */
	boolean tableExists(String tableName) throws Exception {
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
	 * Checks if a column with the given name exists in the table with the given
	 * name. If not, it is created using the given column definition.
	 */
	void checkCreateColumn(String tableName, String columnName, String columnDef)
			throws Exception {
		log.trace("Check if column {} exists in {}", columnName, tableName);
		if (columnExists(tableName, columnName))
			log.trace("column exists");
		else {
			log.info("add column {} to {}", columnName, tableName);
			String stmt = "ALTER TABLE " + tableName + " ADD COLUMN "
					+ columnDef;
			NativeSql.on(database).runUpdate(stmt);
		}
	}

	/**
	 * Returns true if the column with the given name exists in the table with
	 * the given name.
	 */
	boolean columnExists(String tableName, String columnName) throws Exception {
		try (Connection con = database.createConnection()) {
			DatabaseMetaData metaData = con.getMetaData();
			try (ResultSet rs = metaData.getColumns(null, null, "%", "%")) {
				while (rs.next()) {
					String tName = rs.getString(3);
					String cName = rs.getString(4);
					if (tName.equalsIgnoreCase(tableName)
							&& cName.equalsIgnoreCase(columnName))
						return true;
				}
				return false;
			}
		}
	}

	void renameColumn(String table, String oldName, String newName,
			String dataType) throws Exception {
		if (columnExists(table, newName))
			return;
		log.trace("rename column {}.{} to {}", table, oldName, newName);
		if (!columnExists(table, oldName)) {
			log.error("column {}.{} does not exists", table, oldName);
			return;
		}
		String query = null;
		if (database instanceof DerbyDatabase)
			query = "RENAME COLUMN " + table + "." + oldName + " TO " + newName;
		else
			query = "ALTER TABLE " + table + " CHANGE " + oldName + " "
					+ newName + " " + dataType;
		NativeSql.on(database).runUpdate(query);
	}

}
