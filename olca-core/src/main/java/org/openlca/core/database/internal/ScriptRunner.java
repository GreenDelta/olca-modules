package org.openlca.core.database.internal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

/**
 * A class for running a SQL scripts with insert, update, delete, and DDL
 * statements on a database connection.
 */
public class ScriptRunner implements ScriptHandler {

	private final int MAX_BATCH_SIZE = 1000;
	private final IDatabase db;
	private final List<String> statements = new ArrayList<>();

	public ScriptRunner(IDatabase db) {
		this.db = db;
	}

	public void run(InputStream scriptStream, String encoding) throws Exception {
		try {
			var reader = new InputStreamReader(scriptStream, encoding);
			var parser = new ScriptParser(this);
			parser.parse(reader);
			execBatch();
		} catch (Exception e) {
			throw new Exception("Cannot execute script: " + e.getMessage(), e);
		}
	}

	@Override
	public void statement(String query) throws Exception {
		statements.add(query);
		if (statements.size() >= MAX_BATCH_SIZE)
			execBatch();
	}

	private void execBatch() throws Exception {
		if (statements.isEmpty())
			return;
		NativeSql.on(db).batchUpdate(statements);
		statements.clear();
	}
}
