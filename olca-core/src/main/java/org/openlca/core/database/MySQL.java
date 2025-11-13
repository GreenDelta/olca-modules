package org.openlca.core.database;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

public class MySQL implements IDatabase {

	private static final Logger log = LoggerFactory.getLogger(MySQL.class);
	private final String name;
	private final EntityManagerFactory entityFactory;
	private final HikariDataSource connectionPool;
	private File fileDir;

	private MySQL(Config config) {
		this.name = config.database;
		var url = config.jdbcUrl();

		// create the JPA persistence manager
		var jpaConfig = new HashMap<>();
		jpaConfig.put("jakarta.persistence.jdbc.url", url);
		jpaConfig.put("jakarta.persistence.jdbc.user", config.user);
		jpaConfig.put("jakarta.persistence.jdbc.password", config.password);
		jpaConfig.put("jakarta.persistence.jdbc.driver", getDriver());
		jpaConfig.put("eclipselink.classloader", getClass().getClassLoader());
		jpaConfig.put("eclipselink.target-database", "MySQL");
		entityFactory = new PersistenceProvider()
				.createEntityManagerFactory(config.persistenceUnit, jpaConfig);

		// create the connection pool
		var poolConfig = new HikariConfig();
		poolConfig.setJdbcUrl(url);
		poolConfig.setUsername(config.user);
		poolConfig.setPassword(config.password);
		connectionPool = new HikariDataSource(poolConfig);
	}

	@Override
	public File getFileStorageLocation() {
		return fileDir;
	}

	public void setFileStorageLocation(File dir) {
		this.fileDir = dir;
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	public Connection createConnection() {
		try {
			var con = connectionPool.getConnection();
			con.setAutoCommit(false);
			return con;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create connection", e);
		}
	}

	@Override
	public void close() {
		try {
			if (entityFactory.isOpen()) {
				entityFactory.close();
			}
			if (!connectionPool.isClosed()) {
				connectionPool.close();
			}
		} catch (Exception e) {
			Exceptions.unchecked("Failed to close database", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getVersion() {
		return DbUtils.getVersion(this);
	}

	public static boolean containsLibrary(Config config, String name) {
		try (var connectionPool = new HikariDataSource()) {
			connectionPool.setDriverClassName(getDriver());
			connectionPool.setJdbcUrl(config.jdbcUrl());
			connectionPool.setAutoCommit(false);
			try (var con = connectionPool.getConnection(config.user, config.password)) {
				return IDatabase.getLibraries(con).contains(name);
			} catch (SQLException e) {
				log.error("Getting connection from connection pool failed", e);
				// fallback, don't accidently assume library is not present
				return true;
			}
		}
	}

	private static String getDriver() {
		return "org.mariadb.jdbc.Driver";
	}

	public static Config database(String db) {
		return new Config(db);
	}

	public static class Config {

		private final String database;
		private String host = "localhost";
		private String user = "root";
		private String password = "";
		private int port = 3306;
		private String url;
		private String persistenceUnit = "openLCA";

		private Config(String database) {
			this.database = database;
		}

		public Config user(String user) {
			this.user = user;
			return this;
		}

		public Config password(String password) {
			this.password = password;
			return this;
		}

		public Config host(String host) {
			this.host = host;
			return this;
		}

		public Config port(int port) {
			this.port = port;
			return this;
		}

		public Config url(String url) {
			this.url = url;
			return this;
		}

		/**
		 * Optionally set the name of the JPA persistence unit that should be
		 * used. Defaults to {@code openLCA} which is shipped with the openLCA
		 * core modules. You should only set this option when you are sure that
		 * a persistence unit with the given name exists in the classpath.
		 *
		 * @param persistenceUnit
		 *            the name of the JPA persistence unit to be used
		 * @return this configuration
		 */
		public Config persistenceUnit(String persistenceUnit) {
			this.persistenceUnit = persistenceUnit;
			return this;
		}
		
		private String jdbcUrl() {
			return url != null
					? url
					: "jdbc:mysql://" + host + ":"
							+ port + "/" + database;
		}

		public MySQL connect() {
			return new MySQL(this);
		}
	}
}
