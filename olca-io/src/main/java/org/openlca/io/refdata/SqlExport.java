package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.LoggerFactory;

/**
 * Provides a template of an CSV export based on a plain SQL query result.
 */
interface SqlExport extends Export {

	@Override
	default void doIt(CSVPrinter printer, IDatabase db) {
		NativeSql.on(db).query(getQuery(), r -> {
			try {
				var line = createLine(r);
				printer.printRecord(line);
				return true;
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("failed to write line", e);
				return false;
			}
		});
	}

	String getQuery();

	Object[] createLine(ResultSet rs) throws SQLException;

}
