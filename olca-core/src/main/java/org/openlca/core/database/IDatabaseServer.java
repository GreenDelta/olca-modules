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
	 * Creates a database with the given name and type. For the
	 * content-type-flag see the CONTENT_TYPE_* constants in this interface.
	 */
	IDatabase createDatabase(String name, int contentType) throws Exception;

	/** Deletes the given database. */
	void delete(IDatabase database) throws Exception;

	IDatabase connect(DatabaseDescriptor descriptor) throws Exception;

	/**
	 * Runs the updates for the database defined by the descriptor. Does nothing
	 * if the database is up-to-date.
	 */
	void update(DatabaseDescriptor descriptor) throws Exception;

	/**
	 * Indicates if the server is running
	 * 
	 * @return True if the server is running, false otherwise
	 */
	boolean isRunning();

	/**
	 * Shuts down the connection to the data provider
	 * 
	 * @throws DataProviderException
	 */
	void shutdown() throws DataProviderException;

	/**
	 * Imports a database from a script file
	 */
	IDatabase importDatabase(final String name, final File fromScript)
			throws Exception;

	/**
	 * Exports a database to a script file.
	 */
	void exportDatabase(IDatabase database, final File file) throws Exception;

	/** Set up the server connection. */
	void connect() throws Exception;

	/**
	 * Returns the list of descriptors for the available openLCA databases
	 * without creating connection instances to these databases.
	 */
	List<DatabaseDescriptor> getDatabaseDescriptors() throws Exception;

}
