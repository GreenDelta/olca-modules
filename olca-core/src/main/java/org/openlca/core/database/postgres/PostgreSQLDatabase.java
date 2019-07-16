package org.openlca.core.database.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.DatabaseException;
import org.openlca.core.database.DbUtils;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Notifiable;
import org.openlca.core.database.internal.Resource;
import org.openlca.core.database.internal.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDatabase implementation for Postgres database. The URL schema is "jdbc:postgresql://" [host] ":"
 * [port] "/" [database]
 */
public class PostgreSQLDatabase extends Notifiable implements IDatabase {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private EntityManagerFactory entityFactory;
	private String host;
	private String port;
	private String dbName;
	private String user;
	private String password;
	private String url;
	private HikariDataSource connectionPool;
	private final String persistenceUnit;
	private File fileStorageLocation;

	public PostgreSQLDatabase(String host, String port, String dbName, String user, String password)
		throws DatabaseException {
		this(host, port, dbName, user, password, "openLCA");
	}

	public PostgreSQLDatabase(String host, String port, String dbName, String user, String password,
		String persistenceUnit) throws DatabaseException {
		this.persistenceUnit = persistenceUnit;
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.user = user;
		this.password = password;
		this.url = getJdbcUrl();
		connect();
	}

	public String getJdbcUrl() {
		return buildJdbcUrl(this.host, this.port, this.dbName);
	}

	public static String buildJdbcUrl(String host, String port, String dbName) {
		return String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);
	}

	private void connect() throws DatabaseException {
		log.trace("Connect to database postgres: {} @ {}", user, url);
		Map<Object, Object> map = new HashMap<>();
		map.put("javax.persistence.jdbc.url", url);
		map.put("javax.persistence.jdbc.user", user);
		map.put("javax.persistence.jdbc.password", password);
		map.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");

		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", "PostgreSQL");
		log.trace("Connection to jpa {}", map);

		entityFactory = new PersistenceProvider().createEntityManagerFactory(persistenceUnit, map);
		if (entityFactory == null) {
			log.error("failed to initialize entityFactory for postgres");
			return;
		}

		initConnectionPool();
	}

	private void initConnectionPool() throws DatabaseException {
		try {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(url);
			config.setUsername(user);
			config.setPassword(password);
			connectionPool = new HikariDataSource(config);
		} catch (Exception e) {
			log.error("failed to initialize connection pool", e);
			throw new DatabaseException("Could not create a connection", e);
		}
	}

	public static void createDatabase(String host, String port, String dbName, String user,
		String password) throws Exception {
		Logger log = LoggerFactory.getLogger(PostgreSQLDatabase.class);
		String url = buildJdbcUrl(host, port, "postgres");

		try {
			log.debug("Connecting to database {} ...", url);
			Connection conn = DriverManager.getConnection(url, user, password);
			if (databaseExists(conn, dbName)) {
				log.debug("Database {} found. Continuing...", dbName);
				return;
			}

			log.debug("Creating database {}...", dbName);
			Statement stmt = conn.createStatement();

			stmt.executeUpdate("CREATE DATABASE " + dbName);
			log.debug("Database {} created successfully", dbName);
			stmt.close();

			log.debug("Importing schema for database {}...", dbName);
			ScriptRunner runner = new ScriptRunner(
				new PostgreSQLDatabase(host, port, dbName, user, password));
			runner.run(Resource.CURRENT_SCHEMA_POSTGRESQL.getStream(), "utf-8");
			log.debug("Schema imported for database {}", dbName);

			conn.close();

		} catch (Exception e) {
			log.error("Failed to create database", e);
			throw e;
		}
	}

	private static boolean databaseExists(Connection con, String dbName) throws SQLException {
		Logger log = LoggerFactory.getLogger(PostgreSQLDatabase.class);
		log.debug("Checking if database exists {}", dbName);
		String query = String.format("SELECT 1 as result FROM pg_database WHERE datname='%s'", dbName);

		try (Statement stmt = con.createStatement()) {

			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int result = rs.getInt("result");
				return result == 1;
			}
		} catch (SQLException e) {
			log.error("Failed to check if database exists", e);
			throw e;
		}
		return false;
	}

	@Override
	public File getFileStorageLocation() {
		return fileStorageLocation;
	}

	public void setFileStorageLocation(File fileStorageLocation) {
		this.fileStorageLocation = fileStorageLocation;
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	public Connection createConnection() {
		log.trace("create connection postgres: {} @ {}", user, url);
		try {
			if (connectionPool != null) {
				Connection con = connectionPool.getConnection();
				con.setAutoCommit(false);
				return con;
			} else {
				log.warn("no connection pool set up for {}", url);
				return DriverManager.getConnection(url, user, password);
			}
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	@Override
	public void close() {
		log.trace("close database postgres: {} @ {}", user, url);
		try {
			if (entityFactory != null && entityFactory.isOpen()) {
				entityFactory.close();
			}
			if (connectionPool != null) {
				connectionPool.close();
			}
		} catch (Exception e) {
			log.error("failed to close database", e);
		} finally {
			entityFactory = null;
			connectionPool = null;
		}
	}

	@Override
	public String getName() {
		return dbName;
	}

	@Override
	public int getVersion() {
		return DbUtils.getVersion(this);
	}
}
