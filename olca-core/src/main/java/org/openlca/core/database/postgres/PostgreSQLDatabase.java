package org.openlca.core.database.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.persistence.EntityManagerFactory;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.eclipse.persistence.config.TargetDatabase;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.DatabaseException;
import org.openlca.core.database.DbUtils;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
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

	private static Logger log = LoggerFactory.getLogger(PostgreSQLDatabase.class);
	private EntityManagerFactory entityFactory;
	private PostgreSQLConnParams connParams;
	private HikariDataSource connectionPool;
	private final String persistenceUnit;
	private File fileStorageLocation;

	private boolean autoCommit = false;

	public PostgreSQLDatabase(PostgreSQLConnParams params) throws DatabaseException {
		this(params, "openLCA");
	}

	public PostgreSQLDatabase(PostgreSQLConnParams connParams, String persistenceUnit)
		throws DatabaseException {
		this.persistenceUnit = persistenceUnit;
		this.connParams = connParams;
		connect();
	}

	private void connect() throws DatabaseException {
		log.trace("Connect to database postgres: {} @ {}", connParams.getUser(),
			connParams.getJdbcUrl());
		Map<Object, Object> map = new HashMap<>();
		map.put(EntityManagerProperties.JDBC_URL, connParams.getJdbcUrl());
		map.put(EntityManagerProperties.JDBC_USER, connParams.getUser());
		map.put(EntityManagerProperties.JDBC_PASSWORD, connParams.getPassword());
		map.put(EntityManagerProperties.JDBC_DRIVER, "org.postgresql.Driver");

		// Uncomment bellow to generate postgres schema
		// map.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_ONLY);
		// map.put(PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_SQL_SCRIPT_GENERATION);
		// map.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE, "create.sql");
		// map.put(PersistenceUnitProperties.DEPLOY_ON_STARTUP, "true");
		// map.put(PersistenceUnitProperties.APP_LOCATION, "/tmp");

		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", TargetDatabase.PostgreSQL);
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
			config.setJdbcUrl(connParams.getJdbcUrl());
			config.setUsername(connParams.getUser());
			config.setPassword(connParams.getPassword());
			config.setAutoCommit(autoCommit);
			config.setDriverClassName(org.postgresql.Driver.class.getCanonicalName());
			connectionPool = new HikariDataSource(config);
		} catch (Exception e) {
			log.error("failed to initialize connection pool", e);
			throw new DatabaseException("Could not create a connection", e);
		}
	}

	public static PostgreSQLDatabase createDatabase(PostgreSQLConnParams connParams)
		throws Exception {
		PostgreSQLConnParams postgresDbParams = connParams.clone().setDbName("postgres");
		PostgreSQLDatabase postgresDb = new PostgreSQLDatabase(postgresDbParams);
		postgresDb.setAutoCommit(true);

		if (!DbUtils.isValidName(connParams.getDbName())) {
			throw new Exception(String.format(
				"Invalid database name '%s'. See https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS",
				connParams.getDbName()));
		}
		try {
			log.trace("Connecting to database {} ...", postgresDbParams.getDbName());

			if (databaseExists(connParams)) {
				log.trace("Database {} found. Continuing...", connParams.getDbName());
				return new PostgreSQLDatabase(connParams);
			}

			String statement = "CREATE DATABASE " + connParams.getDbName();
			log.trace("run update statement {}", statement);
			Connection con = postgresDb.createConnection();
			Statement stmt = con.createStatement();
			stmt.executeUpdate(statement);
			stmt.close();
			log.trace("Database {} created successfully", connParams.getDbName());

			log.trace("Importing schema for database {}...", connParams.getDbName());
			ScriptRunner runner = new ScriptRunner(new PostgreSQLDatabase(connParams));
			runner.run(Resource.CURRENT_SCHEMA_POSTGRESQL.getStream(), "utf-8");
			log.trace("Schema imported for database {}", connParams.getDbName());

		} catch (Exception e) {
			log.error("Failed to create database", e);
			throw e;
		}

		postgresDb.close();
		return new PostgreSQLDatabase(connParams);
	}

	public static void dropDatabase(PostgreSQLConnParams connParams) throws Exception {
		PostgreSQLConnParams postgresDbParams = connParams.clone().setDbName("postgres");
		PostgreSQLDatabase postgresDb = new PostgreSQLDatabase(postgresDbParams);
		postgresDb.setAutoCommit(true);

		if (!DbUtils.isValidName(connParams.getDbName())) {
			throw new Exception(String.format(
				"Invalid database name '%s'. See https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS",
				connParams.getDbName()));
		}
		log.trace("dropping database {}...", connParams.getDbName());

		StringBuilder killConnectionsSb = new StringBuilder();
		killConnectionsSb.append("SELECT pg_terminate_backend (pg_stat_activity.pid)");
		killConnectionsSb.append(" FROM pg_stat_activity");
		killConnectionsSb.append(" WHERE");
		killConnectionsSb.append(" pid <> pg_backend_pid()");
		killConnectionsSb.append(" AND datname = '");
		killConnectionsSb.append(connParams.getDbName());
		killConnectionsSb.append("'");

		try {
			NativeSql.on(postgresDb).query(killConnectionsSb.toString(), (entry) -> true);
		} catch (Exception e) {
			log.error("Error while terminating connections for " + connParams.getDbName(), e);
			throw e;
		}

		try {
			String statement = "DROP DATABASE IF EXISTS " + connParams.getDbName();
			log.trace("run update statement {}", statement);
			Connection con = postgresDb.createConnection();
			Statement stmt = con.createStatement();
			stmt.executeUpdate(statement);
			stmt.close();
			log.trace("Database {} dropped successfully", connParams.getDbName());
		} catch (Exception e) {
			log.error("Error while terminating connections for " + connParams.getDbName(), e);
			throw e;
		}
		postgresDb.close();
	}

	public static boolean databaseExists(PostgreSQLConnParams connParams) throws Exception {
		log.trace("Checking if database exists {}", connParams);

		PostgreSQLConnParams postgresDbParams = connParams.clone().setDbName("postgres");
		IDatabase postgresDb = new PostgreSQLDatabase(postgresDbParams);

		String query = String
			.format("SELECT 1 as count FROM pg_database WHERE datname='%s'", connParams.getDbName());
		AtomicBoolean databaseExists = new AtomicBoolean(false);
		try {
			NativeSql.on(postgresDb).query(query, (entry) -> {
				databaseExists.set(entry.getInt("count") == 1);
				return true;
			});
		} catch (Exception e) {
			log.error("Error while checking if database " + connParams.getDbName() + " exists", e);
			throw e;
		}

		postgresDb.close();
		return databaseExists.get();
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
		log.trace("create connection postgres: {} @ {}", connParams.getUser(), connParams.getJdbcUrl());
		try {
			if (connectionPool != null) {
				Connection con = connectionPool.getConnection();
				con.setAutoCommit(autoCommit);
				return con;
			} else {
				log.warn("no connection pool set up for {}", connParams.getJdbcUrl());
				return DriverManager
					.getConnection(connParams.getJdbcUrl(), connParams.getUser(), connParams.getPassword());
			}
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	@Override
	public void close() {
		log.trace("close database postgres: {} @ {}", connParams.getUser(), connParams.getJdbcUrl());
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

	public PostgreSQLDatabase setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
		return this;
	}

	@Override
	public String getName() {
		return connParams.getDbName();
	}

	@Override
	public int getVersion() {
		return DbUtils.getVersion(this);
	}
}
