package org.openlca.io.refdata;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractImport {

	protected Logger log = LoggerFactory.getLogger(getClass());
	protected Seq seq;
	protected IDatabase database;
	private final List<CSVRecord> nextBatch = new ArrayList<>();

	public void run(File file, Seq seq, IDatabase db) throws Exception {
		this.seq = seq;
		this.database = db;
		try (var reader = new FileReader(file, StandardCharsets.UTF_8);
				 var parser = new CSVParser(reader, Maps.format())) {
			for (var row : parser) {
				if (isValid(row)) {
					nextBatch.add(row);
				}
				if (nextBatch.size() > 2000) {
					execBatch(db);
				}
			}
			execBatch(db);
		}
	}

	protected abstract boolean isValid(CSVRecord row);

	private void execBatch(IDatabase db) {
		if (nextBatch.isEmpty())
			return;
		try {
			NativeSql.on(db).batchInsert(getStatement(),
				nextBatch.size(), new BatchHandler());
		} catch (Exception e) {
			log.error("failed to execute batch insert", e);
		}
		nextBatch.clear();
	}

	protected abstract String getStatement();

	protected abstract void setValues(
		PreparedStatement statement, CSVRecord values) throws Exception;

	protected void setRef(PreparedStatement stmt, int field, ModelType type,
		String refId) throws Exception {
		if (Strings.isNullOrEmpty(refId)) {
			stmt.setNull(field, Types.BIGINT);
			return;
		}
		long id = seq.get(type, refId);
		if (id != 0) {
			stmt.setLong(field, id);
		} else {
			stmt.setNull(field, Types.BIGINT);
		}
	}

	private class BatchHandler implements NativeSql.BatchUpdateHandler {
		@Override
		public boolean addBatch(int i, PreparedStatement stmt) {
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
