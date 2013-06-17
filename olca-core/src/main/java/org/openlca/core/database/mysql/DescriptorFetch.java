package org.openlca.core.database.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.database.DatabaseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the database descriptors from a server connection.
 */
class DescriptorFetch {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Connection con;

	public DescriptorFetch(Connection con) {
		this.con = con;
	}

	public List<DatabaseDescriptor> doFetch() {
		log.trace("Get database descriptors ");
		String query = "SHOW DATABASES";
		try (ResultSet set = con.createStatement().executeQuery(query)) {
			List<DatabaseDescriptor> descriptors = new ArrayList<>();
			while (set.next()) {
				String databaseName = set.getString(1);
				DatabaseDescriptor descriptor = getDescriptor(databaseName);
				if (descriptor != null)
					descriptors.add(descriptor);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to get database descriptors", e);
			return Collections.emptyList();
		}
	}

	private DatabaseDescriptor getDescriptor(String databaseName) {
		log.trace("Check database {}", databaseName);
		try {
			if (!hasVersionTable(databaseName))
				return null;
			String version = getVersion(databaseName);
			DatabaseDescriptor descriptor = new DatabaseDescriptor();
			descriptor.setName(databaseName);
			descriptor.setVersion(version);
			descriptor.setUpToDate(Version.CURRENT.equals(version));
			return descriptor;
		} catch (Exception e) {
			log.error("Failed to check database " + databaseName, e);
			return null;
		}
	}

	private boolean hasVersionTable(String db) throws Exception {
		String query = "SHOW TABLES FROM " + db;
		try (ResultSet rs = con.createStatement().executeQuery(query)) {
			boolean found = false;
			while (!found && rs.next())
				found = Version.TABLE.equals(rs.getString(1));
			return found;
		}
	}

	private String getVersion(String db) throws Exception {
		String query = "SELECT " + Version.FIELD + " FROM " + db + "."
				+ Version.TABLE;
		try (ResultSet rs = con.createStatement().executeQuery(query)) {
			rs.first();
			return rs.getString(Version.FIELD);
		}
	}

}
