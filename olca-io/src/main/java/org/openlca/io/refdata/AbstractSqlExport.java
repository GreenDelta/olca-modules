package org.openlca.io.refdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.supercsv.io.CsvListWriter;

/**
 * Provides a template of an CSV export based on a plain SQL query result.
 */
abstract class AbstractSqlExport extends AbstractExport {

	@Override
	protected void doIt(final CsvListWriter writer, IDatabase database)
			throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		NativeSql.on(database).query(getQuery(),
				new NativeSql.QueryResultHandler() {
					@Override
					public boolean nextResult(ResultSet resultSet)
							throws SQLException {
						return writeLine(resultSet, writer, count);
					}
				});
		logWrittenCount(count.get());
	}

	private boolean writeLine(ResultSet resultSet, CsvListWriter writer,
			AtomicInteger count) {
		try {
			Object[] line = createLine(resultSet);
			writer.write(line);
			count.incrementAndGet();
			return true;
		} catch (Exception e) {
			log.error("failed to write line", e);
			return false;
		}
	}

	protected abstract String getQuery();

	protected abstract Object[] createLine(ResultSet resultSet)
			throws Exception;

	protected abstract void logWrittenCount(int count);

}
