package org.openlca.core.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public MySQLDatabase(String url, String user, String password) {
		this.url = url;
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
				"openLCA", map);
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	// TODO: use a connection pool here
	public Connection createConnection() {
		log.trace("create connection mysql: {} @ {}", user, url);
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	@Override
	public void close() {
		log.trace("close database mysql: {} @ {}", user, url);
		if (entityFactory != null && entityFactory.isOpen())
			entityFactory.close();
	}

	@Override
	public <T> BaseDao<T> createDao(Class<T> clazz) {
		return new BaseDao<>(clazz, getEntityFactory());
	}

	public String getName() {
		if (url == null)
			return null;
		String[] parts = url.split("/");
		if (parts.length < 2)
			return null;
		return parts[parts.length - 1].trim();
	}
}
