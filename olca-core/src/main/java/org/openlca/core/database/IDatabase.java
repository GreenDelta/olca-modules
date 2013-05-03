package org.openlca.core.database;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.modelprovider.IModelComponent;

public interface IDatabase {

	/**
	 * Creates a native SQL connection to the underlying database. The
	 * connection should be closed from the respective client.
	 */
	Connection createConnection();

	/**
	 * Deletes an object from the data provider
	 * 
	 * @param object
	 *            The object to delete
	 * @throws DataProviderException
	 */
	void delete(final Object object) throws DataProviderException;

	/**
	 * Getter of the name
	 * 
	 * @return The name of the database
	 */
	String getName();

	String getUrl();

	/**
	 * Inserts an object into the data provider
	 * 
	 * @param object
	 *            The object to insert
	 * @throws DataProviderException
	 */
	void insert(final Object object) throws DataProviderException;

	/**
	 * Executes the specified query
	 * 
	 * @param query
	 *            The query to execute
	 * @return The objects found for the query, if no object is found an empty
	 *         array will be returned
	 */
	Object[] query(final String query);

	/**
	 * Loads the object from the data provider, which matches the given class
	 * and id
	 * 
	 * @param <T>
	 *            The class of the requested object
	 * 
	 * @param objectClass
	 *            The class of the object to load
	 * @param id
	 *            The id of the object to load
	 * @return The object found for the given class and id, or null if none
	 *         found
	 * @throws DataProviderException
	 */
	<T> T select(final Class<T> objectClass, final String id)
			throws DataProviderException;

	/**
	 * Loads all objects from the data provider, which matches the given class
	 * and specified properties
	 * 
	 * @param <T>
	 *            The class of the requested object
	 * 
	 * @param objectClass
	 *            The class of the objects to load
	 * @param properties
	 *            The conditions to filter the result. A condition is specified
	 *            by a key and a value separated by '='. e.g:
	 *            "process.name=name of process" or
	 *            "process.exchanges.flow.name=name of flow"
	 * @return The objects found for the given class and conditions, if no
	 *         object is found an empty array will be returned
	 * @throws DataProviderException
	 */
	<T> List<T> selectAll(final Class<T> objectClass,
			final Map<String, Object> properties) throws DataProviderException;

	/**
	 * Loads a descriptor from the data provider, which matches the given class
	 * and id
	 * 
	 * @param objectClass
	 *            The class of the objects to load
	 * @param id
	 *            The id
	 * @return The object descriptor found for the given class and if
	 * @throws DataProviderException
	 */
	IModelComponent selectDescriptor(final Class<?> objectClass, final String id)
			throws DataProviderException;

	/**
	 * Loads all object descriptors from the data provider, which matches the
	 * given class
	 * 
	 * @param objectClass
	 *            The class of the objects to load
	 * @return The object descriptors found for the given class, if no object is
	 *         found an empty array will be returned
	 * @throws DataProviderException
	 */
	IModelComponent[] selectDescriptors(final Class<?> objectClass)
			throws DataProviderException;

	/**
	 * Loads all object descriptors from the data provider, which matches the
	 * given class and specified properties
	 * 
	 * @param objectClass
	 *            The class of the objects to load
	 * @param properties
	 *            The conditions to filter the result. A condition is specified
	 *            by a key and a value separated by '='. e.g:
	 *            "process.name=name of process" or
	 *            "process.exchanges.flow.name=name of flow"
	 * @return The object descriptors found for the given class and properties,
	 *         if no object is found an empty array will be returned
	 * @throws DataProviderException
	 */
	IModelComponent[] selectDescriptors(final Class<?> objectClass,
			final Map<String, Object> properties) throws DataProviderException;

	/**
	 * Updates an object in the data provider
	 * 
	 * @param object
	 *            The object to update
	 * @throws DataProviderException
	 */
	void update(final Object object) throws DataProviderException;

	EntityManagerFactory getEntityFactory();

	<T> BaseDao<T> createDao(Class<T> clazz);

}
