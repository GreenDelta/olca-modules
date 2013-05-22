package org.openlca.core.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.database.BaseDao;
import org.openlca.core.database.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDatabase implementation for MySQL database.
 */
public class Database implements IDatabase {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private EntityManagerFactory entityFactory;
	private ConnectionData data;

	public Database(ConnectionData connectionData) {
		connect(connectionData);
		this.data = connectionData;
	}

	private void connect(ConnectionData data) {
		log.trace("Connect to database {}", data);
		Map<Object, Object> map = new HashMap<>();
		map.put("javax.persistence.jdbc.url", data.getUrl());
		map.put("javax.persistence.jdbc.user", data.getUser());
		map.put("javax.persistence.jdbc.password", data.getPassword());
		map.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", "MySQL");
		entityFactory = data.getPersistenceProvider()
				.createEntityManagerFactory("openLCA", map);
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	public Connection createConnection() {
		try {
			return DriverManager.getConnection(data.getUrl(), data.getUser(),
					data.getPassword());
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	@Override
	public void close() {
		if (entityFactory != null && entityFactory.isOpen())
			entityFactory.close();
	}

	@Override
	public <T> BaseDao<T> createDao(Class<T> clazz) {
		return new BaseDao<>(clazz, getEntityFactory());
	}
}
