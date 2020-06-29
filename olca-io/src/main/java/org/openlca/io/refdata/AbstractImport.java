package org.openlca.io.refdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

abstract class AbstractImport {

	protected Logger log = LoggerFactory.getLogger(getClass());
	protected Seq seq;
	protected IDatabase database;
	private List<List<Object>> nextBatch = new ArrayList<>();

	public void run(File file, Seq seq, IDatabase database) throws Exception {
		this.seq = seq;
		this.database = database;
		CsvPreference pref = new CsvPreference.Builder('"', ';', "\n").build();
		try (FileInputStream fis = new FileInputStream(file);
				// exclude the byte order mark, if there is any
				BOMInputStream bom = new BOMInputStream(fis, false,
						ByteOrderMark.UTF_8);
				InputStreamReader reader = new InputStreamReader(bom, "utf-8");
				BufferedReader buffer = new BufferedReader(reader);
				CsvListReader csvReader = new CsvListReader(buffer, pref)) {
			importFile(csvReader, database);
		}
	}

	private void importFile(CsvListReader csvReader, IDatabase database)
			throws Exception {
		CellProcessor[] processors = getCellProcessors();
		List<Object> values;
		while ((values = next(processors, csvReader)) != null) {
			if (isValid(values))
				nextBatch.add(values);
			if (nextBatch.size() > 2000)
				execBatch(database);
		}
		execBatch(database);
	}

	protected abstract boolean isValid(List<Object> values);

	private List<Object> next(CellProcessor[] processors,
			CsvListReader csvReader) {
		try {
			return csvReader.read(processors);
		} catch (Exception e) {
			log.error("failed to read line " + csvReader.getLineNumber(), e);
			return null;
		}
	}

	private void execBatch(IDatabase database) {
		if (nextBatch.isEmpty())
			return;
		try {
			NativeSql.on(database).batchInsert(getStatement(),
					nextBatch.size(), new BatchHandler());
		} catch (Exception e) {
			log.error("failed to execute batch insert", e);
		}
		nextBatch.clear();
	}

	protected abstract String getStatement();

	protected abstract CellProcessor[] getCellProcessors();

	protected abstract void setValues(PreparedStatement statement,
			List<Object> values) throws Exception;

	private class BatchHandler implements NativeSql.BatchUpdateHandler {
		@Override
		public boolean addBatch(int i, PreparedStatement stmt)
				throws SQLException {
			try {
				setValues(stmt, nextBatch.get(i));
				return true;
			} catch (Exception e) {
				log.error("failed to set values", e);
				return false;
			}
		}
	}

}
