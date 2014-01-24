package org.openlca.core.database.upgrades;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

class UpgradeUtil {

	private IDatabase database;

	private Logger log = LoggerFactory.getLogger(getClass());

	UpgradeUtil(IDatabase database) {
		this.database = database;
	}

	/**
	 * Checks if a table with the given name exists in the
	 * database. If not it is created using the given
	 * table definition.
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

	void checkCreateColumn(String tableName, String columnName,
	                       String columnDef) throws Exception {
		log.trace("Check if column {} exists in {}", columnName, tableName);
		if (columnExists(tableName, columnName))
			log.trace("column exists");
		else {
			log.info("add column {} to {}", columnName, tableName);
			String stmt = "ALTER TABLE " + tableName + " ADD COLUMN " + columnDef;
			NativeSql.on(database).runUpdate(stmt);
		}
	}

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


}
