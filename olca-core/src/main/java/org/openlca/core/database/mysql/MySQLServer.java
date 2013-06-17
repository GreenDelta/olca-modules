/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Mozilla Public License v1.1
 * which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 *
 * Contributors:
 *     	GreenDeltaTC - initial API and implementation
 *		www.greendeltatc.com
 *		tel.:  +49 30 4849 6030
 *		mail:  gdtc@greendeltatc.com
 *******************************************************************************/

package org.openlca.core.database.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;

import org.openlca.core.database.DataProviderException;
import org.openlca.core.database.DatabaseDescriptor;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.IDatabaseServer;
import org.openlca.core.database.internal.ScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.mysql.jdbc.Driver;

public class MySQLServer implements IDatabaseServer {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private ServerApp serverApp;
	private boolean running = false;
	private String url;
	private String user;
	private String password;

	/**
	 * Creates a new server instance with the given connection data. The url
	 * must have the following schema jdbc:mysql://[host]:[port].
	 */
	public MySQLServer(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}

	/**
	 * Creates a new server instance with a wrapped executable. The connection
	 * data are set as follows: url = jdbc:mysql://localhost:3306, user = root
	 * (no password).
	 */
	public MySQLServer(ServerApp app) {
		this("jdbc:mysql://localhost:3306", "root", null);
		this.serverApp = app;
	}

	// TODO: boolean hasDatabase(name)
	public boolean hasDatabase(String name) throws Exception {
		try (Connection con = createConnection()) {
			ResultSet resultSet = con.createStatement().executeQuery(
					"show databases");
			boolean found = false;
			while (!found && resultSet.next()) {
				String db = resultSet.getString(1);
				if (db.equalsIgnoreCase(name))
					found = true;
			}
			resultSet.close();
			return found;
		} catch (Exception e) {
			log.error("Failed to get databases from server", e);
			throw new Exception(e);
		}
	}

	@Override
	public IDatabase createDatabase(String name, int contentType)
			throws Exception {
		log.info("Create database {}, content type = {}", name, contentType);
		if (hasDatabase(name))
			throw new Exception("database " + name + " already exists");
		try (Connection con = createConnection()) {
			con.createStatement().execute("create database " + name);
			con.createStatement().execute("use " + name);
			runScript("current_schema_v1.4.sql", con);
			if (contentType == IDatabaseServer.CONTENT_TYPE_ALL_REF)
				runScript("ref_data_all.sql", con);
			else if (contentType == IDatabaseServer.CONTENT_TYPE_UNITS)
				runScript("ref_data_units.sql", con);
			return new MySQLDatabase(url + "/" + name, user, password);
		} catch (Exception e) {
			log.error("Failed to create database " + name, e);
			throw new Exception(e);
		}
	}

	private void runScript(String resource, Connection con) throws Exception {
		ScriptRunner runner = new ScriptRunner(con);
		runner.run(getClass().getResourceAsStream(resource), "utf-8");
	}

	@Override
	public void delete(IDatabase database) throws Exception {
		if (!(database instanceof MySQLDatabase))
			throw new Exception("the given database is not an MySQL database");
		log.info("Delete database {}", database);
		MySQLDatabase myDb = (MySQLDatabase) database;
		try {
			myDb.close();

		} catch (Exception e) {
			log.error("Could not delete database", e);
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void shutdown() {
		if (serverApp != null) {
			try {
				serverApp.shutdown();
				serverApp = null;
			} catch (Exception e) {
				log.error("Could not shutdown embedded server", e);
			}
		}
		running = false;
	}

	@Override
	public void connect() throws Exception {
		log.trace("Start database server.");
		try {
			DriverManager.registerDriver(new Driver());
			running = true;
		} catch (Exception e) {
			log.error("Could not start embedded MySQL server");
			running = false;
			throw e;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof MySQLServer) {
			MySQLServer other = (MySQLServer) obj;
			return Objects.equal(this.url, other.url)
					&& Objects.equal(this.user, other.user);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.url, this.user);
	}

	@Override
	public IDatabase importDatabase(String dbName, File packageFile)
			throws Exception {
		log.info("Import database: {}", dbName);
		MySQLDatabase db = (MySQLDatabase) createDatabase(dbName,
				CONTENT_TYPE_EMPTY);
		MySQLDatabaseImport databaseImport = new MySQLDatabaseImport(db,
				packageFile);
		databaseImport.run();
		return db;
	}

	@Override
	public void exportDatabase(IDatabase database, File toScriptFile)
			throws DataProviderException {
		if (!(database instanceof MySQLDatabase))
			throw new DataProviderException(
					"The given database is not a MySQL database");
		MySQLDatabase db = (MySQLDatabase) database;
		log.info("Export database {} ", db.getName());
		MySQLDatabaseExport databaseExport = new MySQLDatabaseExport(
				db.createConnection(), toScriptFile);
		databaseExport.run();
	}

	@Override
	public List<DatabaseDescriptor> getDatabaseDescriptors() throws Exception {
		try (Connection con = createConnection()) {
			DescriptorFetch fetch = new DescriptorFetch(con);
			return fetch.doFetch();
		}
	}

	@Override
	public IDatabase connect(DatabaseDescriptor descriptor) throws Exception {
		if (descriptor == null)
			return null;
		log.trace("Connect to database {}", descriptor);
		MySQLDatabase db = new MySQLDatabase(url + "/" + descriptor.getName(),
				user, password);
		return db;
	}

	@Override
	public void update(DatabaseDescriptor descriptor) throws Exception {
		// TODO: not yet implemented
	}

	// TODO: use a connection pool here
	private Connection createConnection() {
		log.trace("create connection mysql: {} @ {}", user, url);
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			log.error("Failed to create database connection", e);
			return null;
		}
	}
}
