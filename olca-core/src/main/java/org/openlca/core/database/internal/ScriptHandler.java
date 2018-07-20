package org.openlca.core.database.internal;

/**
 * The interface for handlers of SQL script events.
 */
interface ScriptHandler {

	void statement(String statement) throws Exception;

}
