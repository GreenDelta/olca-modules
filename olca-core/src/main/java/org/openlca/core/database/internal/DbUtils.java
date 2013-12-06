package org.openlca.core.database.internal;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.NativeSql.QueryResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUtils {

	private DbUtils() {
	}

	public static int getVersion(IDatabase database) {
		try {
			final int[] version = new int[1];
			NativeSql.on(database).query("select version from openlca_version",
					new QueryResultHandler() {
						@Override
						public boolean nextResult(ResultSet result)
								throws SQLException {
							version[0] = result.getInt(1);
							return true;
						}
					});
			return version[0];
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(DbUtils.class);
			log.error("failed to get the database version", e);
			return -1;
		}
	}
}
