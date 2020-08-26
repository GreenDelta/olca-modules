package org.openlca.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NativeSql {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private final int MAX_BATCH_SIZE = 1000;
	private final IDatabase database;

	public static NativeSql on(IDatabase database) {
		return new NativeSql(database);
	}

	private NativeSql(IDatabase database) {
		this.database = database;
	}

	public void query(String query, QueryResultHandler handler) throws SQLException {
		log.trace("execute query {}", query);
		try (Connection con = database.createConnection();
				Statement stmt = con.createStatement();
				ResultSet result = stmt.executeQuery(query)) {
			while (result.next()) {
				boolean b = handler.nextResult(result);
				if (!b)
					break;
			}
		}
	}

	/**
	 * Creates an updateable cursor for the given query.
	 */
	public void updateRows(String query, QueryResultHandler handler) throws SQLException {
		log.trace("execute update {}", query);
		try (Connection con = database.createConnection();
				Statement stmt = con.createStatement(
						ResultSet.TYPE_SCROLL_SENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
				ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				boolean b = handler.nextResult(rs);
				if (!b)
					break;
			}
			con.commit();
		}
	}

	public void runUpdate(String statement) throws SQLException {
		log.trace("run update statement {}", statement);
		try (Connection con = database.createConnection();
				Statement stmt = con.createStatement()) {
			stmt.executeUpdate(statement);
			con.commit();
			log.trace("update done");
		}
	}

	public void batchInsert(String sql, int size, BatchInsertHandler fn)
			throws SQLException {
		log.trace("execute batch insert {}", sql);
		if (size <= 0) {
			log.trace("size {} <= 0; nothing to do", size);
			return;
		}
		try (Connection con = database.createConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			insertRows(size, fn, ps);
			con.commit();
			log.trace("inserts done");
		}
	}

	private void insertRows(int size, BatchInsertHandler fn,
			PreparedStatement ps) throws SQLException {
		for (int i = 0; i < size; i++) {
			boolean b = fn.addBatch(i, ps);
			if (!b) {
				log.trace("stop inserting at {} of {}", i, size);
				break;
			}
			ps.addBatch();
			if (i % MAX_BATCH_SIZE == 0) {
				int[] rows = ps.executeBatch();
				log.trace("executed batch with {} rows", rows.length);
			}
		}
		int[] rows = ps.executeBatch();
		log.trace("executed batch with {} rows", rows.length);
	}

	public void batchUpdate(Iterable<String> statements) throws SQLException {
		log.trace("execute batch update");
		try (Connection con = database.createConnection();
				Statement stmt = con.createStatement()) {
			int batchSize = 0;
			for (String statement : statements) {
				stmt.addBatch(statement);
				if (batchSize % MAX_BATCH_SIZE == 0) {
					int[] s = stmt.executeBatch();
					log.trace("{} statements executed", s.length);
				}
				batchSize++;
			}
			int[] s = stmt.executeBatch();
			log.trace("{} statements executed", s.length);
			con.commit();
		}
	}

	public interface BatchInsertHandler {

		boolean addBatch(int i, PreparedStatement stmt) throws SQLException;

	}

	public interface QueryResultHandler {

		boolean nextResult(ResultSet result) throws SQLException;

	}

}
