package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

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
		final AtomicInteger count = new AtomicInteger(0);
		NativeSql.on(db).query(getQuery(), r -> {
			try {
				Object[] line = createLine(r);
				printer.printRecord(line);
				count.incrementAndGet();
				return true;
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("failed to write line", e);
				return false;
			}
		});
		logWrittenCount(count.get());
	}

	String getQuery();

	Object[] createLine(ResultSet resultSet) throws Exception;

	void logWrittenCount(int count);

}
