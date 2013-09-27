package org.openlca.shell;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.internal.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public void execFile(Shell shell, String[] args) {
		if (shell.getDatabase() == null) {
			log.error("you do not have a database connection");
			return;
		}
		if (args.length < 1 || args[0] == null) {
			log.error("a file name is expected as argument");
			return;
		}
		File file = new File(args[0]);
		if (!file.exists()) {
			log.error("the given file does not exist");
			return;
		}
		runScript(shell, args, file);
	}

	private void runScript(Shell shell, String[] args, File file) {
		IDatabase db = shell.getDatabase();
		try {
			String encoding = fetchEncoding(args);
			log.info("import sql-file {} using encoding {}", file, encoding);
			FileInputStream in = new FileInputStream(file);
			ScriptRunner runner = new ScriptRunner(db);
			runner.run(in, encoding);
			log.info("import done");
		} catch (Exception e) {
			log.error("failed to execute sql file", e);
		}
	}

	private String fetchEncoding(String[] args) {
		if (args.length < 2 || args[1] == null)
			return System.getProperty("file.encoding");
		return args[1];
	}

	public void exec(Shell shell, String sqlStatement) {
		IDatabase database = shell.getDatabase();
		if (database == null) {
			log.error("you have to connect to a database first");
			return;
		}
		if (sqlStatement == null) {
			log.error("invalid sql statement");
			return;
		}
		String stmt = sqlStatement.trim().toLowerCase();
		if (stmt.startsWith("select ") || stmt.startsWith("show "))
			runSelect(database, sqlStatement);
		else
			runUpdate(database, sqlStatement);
	}

	private void runSelect(IDatabase database, String query) {
		log.info("run select statement {}", query);
		try (Connection con = database.createConnection()) {
			List<String[]> table = new ArrayList<>();
			ResultSet result = con.createStatement().executeQuery(query);
			String[] fields = getFields(result);
			table.add(fields);
			while (result.next()) {
				String[] row = new String[fields.length];
				table.add(row);
				for (int i = 0; i < fields.length; i++) {
					String field = fields[i];
					Object o = result.getObject(field);
					if (o != null)
						row[i] = o.toString();
				}
			}
			result.close();
			new TablePrinter(System.out).print(table);
		} catch (Exception e) {
			log.error("failed to execute query", e);
		}
	}

	private String[] getFields(ResultSet result) throws Exception {
		ResultSetMetaData metaData = result.getMetaData();
		String[] fields = new String[metaData.getColumnCount()];
		for (int i = 0; i < fields.length; i++) {
			fields[i] = metaData.getColumnLabel(i + 1);
		}
		return fields;
	}

	private void runUpdate(IDatabase database, String stmt) {
		log.info("run update statement {}", stmt);
		try (Connection con = database.createConnection()) {
			int count = con.createStatement().executeUpdate(stmt);
			con.commit();
			log.info("{} rows updated", count);
		} catch (Exception e) {
			log.error("failed to execute update statement", e);
		}
	}

}
