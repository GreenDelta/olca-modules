package org.openlca.core.database.references;

import java.sql.ResultSet;
import java.sql.SQLException;

final class ResultSetWrapper {

	private ResultSet set;

	ResultSetWrapper(ResultSet set) {
		this.set = set;
	}

	boolean isNull(int column) {
		try {
			return set.getObject(column) == null;
		} catch (SQLException e) {
			Search.log.error("Error receiving an object from native sql result set", e);
			return true;
		}		
	}
	
	long getLong(int column) {
		try {
			return set.getLong(column);
		} catch (SQLException e) {
			Search.log.error("Error receiving a long from native sql result set", e);
			return 0L;
		}
	}

	String getString(int column) {
		try {
			String value = set.getString(column);
			if (value == null)
				return "";
			return value;
		} catch (SQLException e) {
			Search.log.error("Error receiving a string from native sql result set", e);
			return "";
		}
	}

	boolean getBoolean(int column) {
		try {
			return set.getBoolean(column);
		} catch (SQLException e) {
			Search.log.error("Error receiving a boolean from native sql result set", e);
			return false;
		}
	}
}