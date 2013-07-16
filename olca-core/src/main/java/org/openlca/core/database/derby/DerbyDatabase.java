package org.openlca.core.database.derby;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.openlca.core.database.BaseDao;
import org.openlca.core.database.DatabaseContent;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.internal.Resource;
import org.openlca.core.database.internal.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class DerbyDatabase implements IDatabase {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EntityManagerFactory entityFactory;
	private String url;
	private File folder;
	private boolean closed = false;
	private BoneCP connectionPool;

	public DerbyDatabase(File folder) {
		this.folder = folder;
		boolean create = !folder.exists();
		log.info("initialize database folder {}, create={}", folder, create);
		url = "jdbc:derby:" + folder.getAbsolutePath().replace('\\', '/');
		log.trace("database url: {}", url);
		try {
			DriverManager.registerDriver(new EmbeddedDriver());
		} catch (Exception e) {
			throw new RuntimeException("Could not load driver", e);
		}
		if (create)
			createNew(url);
		connect();
	}

	private void createNew(String url) {
		log.info("create new database {}", url);
		try {
			Connection con = DriverManager.getConnection(url + ";create=true");
			ScriptRunner runner = new ScriptRunner(con);
			runner.run(Resource.CURRENT_SCHEMA_DERBY.getStream(), "utf-8");
			con.close();
		} catch (Exception e) {
			log.error("failed to create database", e);
		}
	}

	/** Fill the database with the given content. */
	public void fill(DatabaseContent content) {
		if (content == null || content == DatabaseContent.EMPTY)
			return;
		log.trace("fill database with content: {}", content);
		Resource resource = null;
		if (content == DatabaseContent.ALL_REF_DATA)
			resource = Resource.REF_DATA_ALL;
		else if (content == DatabaseContent.UNITS)
			resource = Resource.REF_DATA_UNITS;
		if (resource == null)
			return;
		try (Connection con = createConnection()) {
			ScriptRunner runner = new ScriptRunner(con);
			runner.run(resource.getStream(), "utf-8");
			con.commit();
		} catch (Exception e) {
			log.error("failed to fill database with  content", e);
		}
	}

	private void connect() {
		log.trace("connect to database: {}", url);
		Map<Object, Object> map = new HashMap<>();
		map.put("javax.persistence.jdbc.url", url);
		map.put("javax.persistence.jdbc.driver",
				"org.apache.derby.jdbc.EmbeddedDriver");
		map.put("eclipselink.classloader", getClass().getClassLoader());
		map.put("eclipselink.target-database", "Derby");
		entityFactory = new PersistenceProvider().createEntityManagerFactory(
				"openLCA", map);
		initConnectionPool();
	}

	private void initConnectionPool() {
		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(url);
			connectionPool = new BoneCP(config);
		} catch (Exception e) {
			log.error("failed to initialize connection pool", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (closed)
			return;
		log.trace("close database: {}", url);
		if (entityFactory != null && entityFactory.isOpen())
			entityFactory.close();
		if (connectionPool != null)
			connectionPool.shutdown();
		try {
			// TODO: single database shutdown throws unexpected
			// error in eclipse APP - close all connections here
			// DriverManager.getConnection(url + ";shutdown=true");
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			// a normal shutdown of derby throws an SQL exception
			// with error code 50000 (for single database shutdown
			// 45000), otherwise an error occurred
			log.info("exception: {}", e.getErrorCode());
			if (e.getErrorCode() != 45000 && e.getErrorCode() != 50000)
				log.error(e.getMessage(), e);
			else {
				closed = true;
				log.info("database closed");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public Connection createConnection() {
		log.trace("create connection: {}", url);
		try {
			if (connectionPool != null)
				return connectionPool.getConnection();
			else {
				log.warn("no connection pool set up for {}", url);
				return DriverManager.getConnection(url);
			}
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}

	@Override
	public EntityManagerFactory getEntityFactory() {
		return entityFactory;
	}

	@Override
	public <T> BaseDao<T> createDao(Class<T> clazz) {
		return new BaseDao<>(clazz, getEntityFactory());
	}

	@Override
	public String getName() {
		return folder.getName();
	}

	/** Closes the database and deletes the underlying folder. */
	public void delete() throws Exception {
		if (!closed)
			close();
		delete(folder);
	}

	private void delete(File folder) {
		log.trace("delete folder {}", folder);
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				delete(f);
			f.delete();
		}
		boolean b = folder.delete();
		log.trace("folder {} deleted? -> {}", folder, b);
	}

}
