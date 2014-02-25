package org.openlca.io.refdata;

import au.com.bytecode.opencsv.CSVReader;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

class CategoryImport {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Seq seq;
	private List<String[]> lines = new ArrayList<>();

	public void run(File file, Seq seq, IDatabase database) throws Exception {
		this.seq = seq;
		try (FileInputStream fis = new FileInputStream(file);
		     InputStreamReader reader = new InputStreamReader(fis, "utf-8");
		     BufferedReader buffer = new BufferedReader(reader);
		     CSVReader csvReader = new CSVReader(buffer, ';', '"')) {
			String[] line;
			while ((line = csvReader.readNext()) != null) {
				checkAddLine(line);
				if (lines.size() > 2000)
					writeLines(database);
			}
			writeLines(database);
		}
	}

	private void writeLines(IDatabase database) throws Exception {
		if (lines.isEmpty())
			return;
		String statement = "insert into tbl_categories (id, ref_id, name, " +
				"description, model_type, f_parent_category) values (?, ?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(statement, lines.size(),
				new NativeSql.BatchInsertHandler() {
					@Override
					public boolean addBatch(int i, PreparedStatement preparedStatement)
							throws SQLException {
						insertLine(i, preparedStatement);
						return true;
					}
				});
		lines.clear();
	}

	private void insertLine(int i, PreparedStatement statement) {
		try {
			String[] line = lines.get(i);
			String refId = line[0];
			long id = seq.get(ModelType.CATEGORY, refId);
			String parentRefId = line[4];
			Long parentId = null;
			if(parentRefId != null)
				parentId = seq.get(ModelType.CATEGORY, parentRefId);
			statement.setLong(1, id);
			statement.setString(2, refId);
			statement.setString(3, line[1]);
			statement.setString(4, line[2]);
			statement.setString(5, line[3]);
			if(parentId != null)
				statement.setLong(6,parentId);
			else
				statement.setNull(6, Types.BIGINT);
		} catch (Exception e) {
			log.error("failed to insert line " + i, e);
		}
	}

	private void checkAddLine(String[] line) {
		if (line == null || line.length < 5)
			return;
		for(int i = 0; i < line.length; i++) {
			if(line[i] == null)
				continue;
			if(line[i].trim().isEmpty())
				line[i] = null;
		}
		String refId = line[0];
		if (seq.isInDatabase(ModelType.CATEGORY, refId))
			return;
		lines.add(line);
	}
}
