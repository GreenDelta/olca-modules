package org.openlca.core.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.DatabaseException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.internal.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * IDatabase implementation for MySQL database. The URL schema is
 * "jdbc:mysql://" [host] ":" [port] "/" [database]
 */
public class MySQLDatabase implements IDatabase {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private EntityManagerFactory entityFactory;
	private String url;
	private String user;
	private String password;
	private BoneCP connectionPool;
	private final String persistenceUnit;

	public MySQLDatabase(String url, String user, String password) {
		this(url, user, password, "openLCA");
	}

	public MySQLDatabase(String url, String user, String password,
			String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
		this.url = url;
		if (!this.url.contains("rewriteBatchedStatements")
				&& this.url.contains("useServerPrepStmts")) {
			this.url += "&rewriteBatchedStatements=true"
					+ "&useServerPrepStmts=false";
			log.trace("modified URL optimized for batch updates: {}", this.url);
		}
		this.user = user;
		this.password = password;
		connect();
	}

	private void connect() {
		log.trace("Connect to database mysql: {} @ {}", user, url);
		Map<Object, Object> map = new HashMap<>();
		map.put("javax.persistence.jdbc.url", url);
		map.put("javax.persistence.jdbc.user", user);
		map.put("javax.persistence.jdbc.password", password);
		map.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", "MySQL");
		entityFactory = new PersistenceProvider().createEntityManagerFactory(
				persistenceUnit, map);
		initConnectionPool();
	}

	private void initConnectionPool() {
		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(url);
			config.setUser(user);
			config.setPassword(password);
			connectionPool = new BoneCP(config);
		} catch (Exception e) {
			log.error("failed to initialize connection pool", e);
			throw new DatabaseException("Could not create a connection", e);
		}
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	public Connection createConnection() {
		log.trace("create connection mysql: {} @ {}", user, url);
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
		log.trace("close database mysql: {} @ {}", user, url);
		try {
			if (entityFactory != null && entityFactory.isOpen())
				entityFactory.close();
			if (connectionPool != null)
				connectionPool.shutdown();
		} catch (Exception e) {
			log.error("failed to close database", e);
		} finally {
			entityFactory = null;
			connectionPool = null;
		}
	}

	@Override
	public <T> BaseDao<T> createDao(Class<T> clazz) {
		return new BaseDao<>(clazz, this);
	}

	@Override
	public String getName() {
		if (url == null)
			return null;
		String[] parts = url.split("/");
		if (parts.length < 2)
			return null;
		return parts[parts.length - 1].trim();
	}

	@Override
	public int getVersion() {
		return DbUtils.getVersion(this);
	}
}
