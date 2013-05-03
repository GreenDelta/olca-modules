/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.database;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Specifies the required functionality of a connection to an openLCA data
 * provider
 * 
 * @author Sebastian Greve
 * 
 */
public interface IDatabaseServer {

	/** Content type constant of a database: empty database. */
	int CONTENT_TYPE_EMPTY = 0;

	/** Content type constant of a database: units and flow properties only. */
	int CONTENT_TYPE_UNITS = 1;

	/** Content type constant of a database: all reference data. */
	int CONTENT_TYPE_ALL_REF = 2;

	/**
	 * The key for the property 'database' of a connection. This key is intended
	 * to be used in the map parameter of the 'setUp' method of this class
	 */
	String DATABASE = "database";

	/**
	 * The canonical name of the driver class
	 */
	String DRIVER_CLASS = "driver";

	/**
	 * The key for the property 'embedded' of a connection. This key is intended
	 * to be used in the map parameter of the 'setUp' method of this class
	 */
	String EMBEDDED = "embedded";

	/**
	 * The key for the property 'host' of a connection. This key is intended to
	 * be used in the map parameter of the 'setUp' method of this class (e.g.
	 * 'host : localhost').
	 */
	String HOST = "host";

	/**
	 * The key for the property 'password' of a connection. This key is intended
	 * to be used in the map parameter of the 'setUp' method of this class.
	 */
	String PASSWORD = "password";

	/**
	 * The key for the property 'port' of a connection. This key is intended to
	 * be used in the map parameter of the 'setUp' method of this class (e.g.
	 * 'port : 3306').
	 */
	String PORT = "port";

	/**
	 * The key for the property 'user' of a connection. This key is intended to
	 * be used in the map parameter of the 'setUp' method of this class.
	 */
	String USER = "user";

	/**
	 * Creates a database with the given name and type. For the
	 * content-type-flag see the CONTENT_TYPE_* constants in this interface.
	 */
	IDatabase createDatabase(String name, int contentType) throws Exception;

	/** Deletes the given database. */
	void delete(IDatabase database) throws Exception;

	/** Get all databases to which a connection was established. */
	List<IDatabase> getConnectedDatabases();

	IDatabase connect(DatabaseDescriptor descriptor) throws Exception;

	/**
	 * Runs the updates for the database defined by the descriptor. Does nothing
	 * if the database is up-to-date.
	 */
	void update(DatabaseDescriptor descriptor) throws Exception;

	/**
	 * Gets the name of the data provider
	 * 
	 * @param includingProperties
	 *            Attaches the properties to identify the server instance
	 * @return The name of the data provider
	 */
	String getName(final boolean includingProperties);

	/**
	 * Gets the properties of the data provider
	 * 
	 * @return The properties to set up the data provider
	 */
	Map<String, String> getProperties();

	/**
	 * Indicates if the server is running
	 * 
	 * @return True if the server is running, false otherwise
	 */
	boolean isRunning();

	/**
	 * Sets the properties of the data provider
	 * 
	 * @param properties
	 *            The properties to set up the data provider
	 */
	void setProperties(final Map<String, String> properties);

	/**
	 * Shuts down the connection to the data provider
	 * 
	 * @throws DataProviderException
	 */
	void shutdown() throws DataProviderException;

	/**
	 * Imports a database from a script file
	 * 
	 * @param name
	 *            The name of the database
	 * @param fromScript
	 *            The file containing the database information
	 * @return The imported database
	 * @throws DataProviderException
	 */
	IDatabase importDatabase(final String name, final File fromScript)
			throws DataProviderException;

	/**
	 * Exports a database to a script file
	 * 
	 * @param name
	 *            The name of the database
	 * @param toScript
	 *            The file to export the database to
	 * @throws DataProviderException
	 */
	void exportDatabase(IDatabase database, final File file)
			throws DataProviderException;

	/** Set up the server connection. */
	void connect() throws Exception;

	/**
	 * Returns the list of descriptors for the available openLCA databases
	 * without creating connection instances to these databases.
	 */
	List<DatabaseDescriptor> getDatabaseDescriptors();

	boolean isEmbedded();

}
