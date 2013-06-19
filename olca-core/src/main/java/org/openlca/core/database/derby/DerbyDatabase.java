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
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.internal.Resource;
import org.openlca.core.database.internal.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbyDatabase implements IDatabase {

	private Logger log = LoggerFactory.getLogger(getClass());
	private EntityManagerFactory entityFactory;
	private String url;
	private File folder;
	private boolean closed = false;

	public DerbyDatabase(File folder) {
		this.folder = folder;
		boolean create = !folder.exists();
		log.info("initialize database @ {}, create={}", folder, create);
		url = "jdbc:derby:" + folder.getAbsolutePath();
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
	}

	@Override
	public void close() throws IOException {
		log.trace("close database: {}", url);
		if (entityFactory != null && entityFactory.isOpen())
			entityFactory.close();
		try {
			DriverManager.getConnection(url + ";shutdown=true");
		} catch (SQLException e) {
			// a normal shutdown of derby throws an SQL exception
			// with error code 50000 (for single database shutdown
			// 45000), otherwise an error occurred
			log.info("exception: {}", e.getErrorCode());
			if (e.getErrorCode() != 45000)
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
	// TODO: use a connection pool here
	public Connection createConnection() {
		log.trace("create connection: {}", url);
		try {
			return DriverManager.getConnection(url);
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
