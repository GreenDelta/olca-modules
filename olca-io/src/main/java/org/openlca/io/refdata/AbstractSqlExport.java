package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.supercsv.io.CsvListWriter;

/**
 * Provides a template of an CSV export based on a plain SQL query result.
 */
abstract class AbstractSqlExport extends AbstractExport {

	@Override
	protected void doIt(final CsvListWriter writer, IDatabase db) {
		final AtomicInteger count = new AtomicInteger(0);
		NativeSql.on(db).query(getQuery(), r -> {
			try {
				Object[] line = createLine(r);
				writer.write(line);
				count.incrementAndGet();
				return true;
			} catch (Exception e) {
				log.error("failed to write line", e);
				return false;
			}
		});
		logWrittenCount(count.get());
	}

	protected abstract String getQuery();

	protected abstract Object[] createLine(ResultSet resultSet)
			throws Exception;

	protected abstract void logWrittenCount(int count);

}
