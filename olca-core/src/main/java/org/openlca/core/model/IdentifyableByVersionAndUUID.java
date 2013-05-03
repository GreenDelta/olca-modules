package org.openlca.core.model;

/**
 * Identification strategy analog ILCD. Goal is to be able to compare results
 * that differ only by the versions of one process (for example). Or to switch
 * different version sets for objects in the database, so that model evolution
 * to new data versions becomes possible.
 * <p>
 * For this to work need to have version-snapshot possibility, so that the
 * active versions can be reestablished. As well as identifying new versions of
 * used processes.
 * <p>
 * Problem(s): What about new 'versions' of entities that come with a new UUID?
 * 
 * @author Georg Koester
 */
public interface IdentifyableByVersionAndUUID {

	String getId();

	String getUUID();

	String getVersion();
}
