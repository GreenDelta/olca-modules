package org.openlca.core.database.descriptors;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DescriptorReader {

	String query();

	default long getId(ResultSet r) throws SQLException {
		return r.getLong(1);
	}

	default String getRefIf(ResultSet r) throws SQLException {
		return r.getString(2);
	}

	default String getName(ResultSet r) throws SQLException {
		return r.getString(3);
	}

	default long getVersion(ResultSet r) throws SQLException {
		return r.getLong(4);
	}

	default long getLastChange(ResultSet r) throws SQLException {
		return r.getLong(5);
	}

	default Long getCategory(ResultSet r) throws SQLException {
		var id = r.getLong(6);
		return r.wasNull() ? null : id;
	}

	default String getLibrary(ResultSet r) throws SQLException {
		return r.getString(7);
	}

	default String getTags(ResultSet r) throws SQLException {
		return r.getString(8);
	}
}
