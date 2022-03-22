package org.openlca.core.database;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NativeSql {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final int MAX_BATCH_SIZE = 1000;
	private final IDatabase db;

	public static NativeSql on(IDatabase database) {
		return new NativeSql(database);
	}

	private NativeSql(IDatabase db) {
		this.db = db;
	}

	public static String stringOf(Clob clob) {
		if (clob == null)
			return null;
		try {
			return clob.getSubString(1, (int) clob.length());
		} catch (SQLException e) {
			throw new RuntimeException("failed to read string from Clob", e);
		}
	}

	public void query(String query, QueryResultHandler handler) {
		log.trace("execute query {}", query);
		try (var con = db.createConnection();
			 var stmt = con.createStatement();
			 var result = stmt.executeQuery(query)) {
			while (result.next()) {
				if (!handler.accept(result))
					break;
			}
		} catch (SQLException e) {
			throw new RuntimeException("query failed: " + query, e);
		}
	}

	/**
	 * Executes the given query as prepared statement. It will set the string
	 * parameters in the order as they are provided. You should always use this
	 * function instead of the non-parameterized version when there is a chance
	 * for SQL injection for some string values.
	 *
	 * @param sql a parameterized query
	 * @param params the parameters of the values
	 * @param fn the result handler
	 */
	public void query(String sql, List<String> params, QueryResultHandler fn) {
		log.trace("execute parameterized query {}", sql);
		try (var con = db.createConnection();
				 var stmt = con.prepareStatement(sql)) {
			int i = 1;
			for (var param : params) {
				stmt.setString(i, param);
				i++;
			}
			if (!stmt.execute())
				return;
			try (var result = stmt.getResultSet()) {
				while (result.next()) {
					if (!fn.accept(result))
						break;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("query failed: " + sql, e);
		}
	}

	/**
	 * Creates an updateable cursor for the given query.
	 */
	public void updateRows(String query, QueryResultHandler handler) {
		log.trace("execute update {}", query);
		try (var con = db.createConnection();
			 var stmt = con.createStatement(
					 ResultSet.TYPE_SCROLL_INSENSITIVE,
					 ResultSet.CONCUR_UPDATABLE);
			 var rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				boolean b = handler.accept(rs);
				if (!b)
					break;
			}
			con.commit();
			db.clearCache();
		} catch (SQLException e) {
			throw new RuntimeException("update failed: " + query, e);
		}
	}

	public void runUpdate(String sql) {
		log.trace("run update statement {}", sql);
		try (var con = db.createConnection();
			 var stmt = con.createStatement()) {
			stmt.executeUpdate(sql);
			con.commit();
			log.trace("update done");
			db.clearCache();
		} catch (SQLException e) {
			throw new RuntimeException("update failed: " + sql, e);
		}
	}

	public void update(String sql, UpdateHandler fn) {
		try (var con = db.createConnection();
			 var stmt = con.prepareStatement(sql)) {
			fn.accept(stmt);
			stmt.executeUpdate();
			con.commit();
			db.clearCache();
		} catch (SQLException e) {
			throw new RuntimeException("updated failed: " + sql, e);
		}
	}

	public void batchInsert(String sql, int size, BatchUpdateHandler fn) {
		log.trace("execute batch insert {}", sql);
		if (size <= 0) {
			log.trace("size {} <= 0; nothing to do", size);
			return;
		}
		try (var con = db.createConnection();
			 var ps = con.prepareStatement(sql)) {
			insertRows(size, fn, ps);
			con.commit();
			log.trace("inserts done");
			db.clearCache();
		} catch (SQLException e) {
			throw new RuntimeException("batch insert failed: " + sql, e);
		}
	}

	private void insertRows(int size, BatchUpdateHandler fn,
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
		try (var con = db.createConnection();
			 var stmt = con.createStatement()) {
			int batchSize = 0;
			for (var statement : statements) {
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
			db.clearCache();
		}
	}

	@FunctionalInterface
	public interface BatchUpdateHandler {
		boolean addBatch(int i, PreparedStatement stmt) throws SQLException;
	}

	@FunctionalInterface
	public interface UpdateHandler {
		void accept(PreparedStatement stmt) throws SQLException;
	}

	@FunctionalInterface
	public interface QueryResultHandler {
		boolean accept(ResultSet result) throws SQLException;
	}

}
