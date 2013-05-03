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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.modelprovider.IModelComponent;
import org.openlca.core.model.modelprovider.ModelComponentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link IDatabaseServer} for JPA persistence
 * supported providers
 * 
 * @author Sebastian Greve
 * 
 */
public abstract class JPADatabase implements IDatabase {

	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private Category rootCategory;

	/**
	 * Returns the root category with id = 'root', name = the value of the
	 * database-field and a child category for each model component registered.
	 */
	private Category getCategory(String id) throws DataProviderException {
		Category category = null;
		EntityManager entityManager = newManager();
		try {
			if (rootCategory == null) {

				rootCategory = new Category();
				rootCategory.setId("root");
				rootCategory.setName("root");

				// load the model components
				// get or create the top categories for the respective model
				// components
				HashMap<Integer, Category> topCategories = new HashMap<>();
				for (String component : ModelComponentRegistry.getRegistry()
						.getModelComponents()) {
					String clazz = ModelComponentRegistry.getRegistry()
							.getClassForName(component).getCanonicalName();
					Category topCategory = entityManager.find(Category.class,
							clazz);
					if (topCategory == null) {
						topCategory = new Category(clazz, component, clazz);
						entityManager.getTransaction().begin();
						entityManager.persist(topCategory);
						entityManager.getTransaction().commit();
					} else {
						topCategory.setName(component);
					}
					int level = ModelComponentRegistry.getRegistry()
							.getLevelForName(component);
					topCategories.put(level, topCategory);
				}

				// sort the top-categories and add them to the root
				List<Integer> ids = new ArrayList<>();
				ids.addAll(topCategories.keySet());
				Collections.sort(ids);
				for (Integer i : ids) {
					rootCategory.add(topCategories.get(i));
					topCategories.get(i).setParentCategory(rootCategory);
				}
				update(rootCategory);
			} // if

			if (id != null) {
				if (id.equals(rootCategory.getId())) {
					category = rootCategory;
				} else {
					category = searchForCategory(rootCategory, id);
				}
			} else {
				category = rootCategory;
			}
		} catch (Exception e) {
			throw new DataProviderException(e.getMessage(), e);
		}
		if (entityManager.isOpen()) {
			entityManager.close();
		}

		return category;
	}

	/**
	 * Creates a SQL-condition of a WHERE-clause. e.g.: "city = 'Berlin'" ;
	 * EXISTS(SELECT t1 FROM Exchange t1 WHERE [recursive call of getCondition]
	 * AND t1 MEMBER OF t0.exchanges)
	 * 
	 * @param clazz
	 *            the result class of the query
	 * @param property
	 *            the property for which a condition should be created
	 * @param value
	 *            the value of the condition
	 * @param type
	 *            The type of the field
	 * @param strict
	 *            if true, only exact values are true, else the LIKE phrase is
	 *            used
	 * @param objectNumber
	 *            the actual recursion level
	 * @param parentNumber
	 *            the parent recursion level
	 * @return the condition as String
	 */
	private String getCondition(Class<?> clazz, String property, Object value,
			Class<?> type, boolean strict, int objectNumber, int parentNumber) {
		String condition = "";
		value = value.toString().replace("'", "''");
		// the property until (including) the first list. e.g:
		// 'flow.flowProperties' (from property
		// 'flow.flowProperties.flowProperty.unitGroup.name')
		String propertyUntilFirstListField = getPropertyUntilFirstListField(
				clazz, property);
		if (propertyUntilFirstListField == null) {
			// no list field exists
			condition += "t" + parentNumber + "." + property;
			if (value.equals("NULL")) {
				condition += " IS NULL";
			} else {
				condition += strict ? " = " : " LIKE ";
				if (type == String.class || type.isEnum()) {
					condition += strict ? "'" + value + "'" : "'%" + value
							+ "%'";
				} else {
					condition += value;
				}
			}
		} else {
			// a list field exists
			String propertyAfterFirstListField = property
					.substring(propertyUntilFirstListField.length() + 1);
			// the generic type of the list field
			Class<?> listFieldType = getListFieldType(clazz, property);
			condition += "EXISTS(SELECT t"
					+ objectNumber
					+ " FROM "
					+ listFieldType.getSimpleName()
					+ " t"
					+ objectNumber
					+ " WHERE "
					+ getCondition(listFieldType, propertyAfterFirstListField,
							value, type, strict, objectNumber + 1, objectNumber)
					+ " AND t" + objectNumber + " MEMBER OF t" + parentNumber
					+ "." + propertyUntilFirstListField + ")";
		}
		return condition;
	}

	/**
	 * Gets the declared field in the given class for the given property
	 * 
	 * @param clazz
	 *            the class which should contain the property
	 * @param prop
	 *            the property the declared field is needed for
	 * @return the declared field in the given class for the given property
	 */
	private Field getDeclaredField(Class<?> clazz, String prop) {
		String property = prop;
		Field field = null;
		if (property != null && clazz != null) {
			int i = 0;
			Class<?> c = clazz;
			String fieldName = "";
			while (!fieldName.equals(property)) {
				// if (fieldName.equals(property)) the field was found or does
				// not exist
				fieldName = property;
				if (property.contains(".")) {
					fieldName = property.substring(0, property.indexOf('.'));
					property = property.substring(property.indexOf('.') + 1);
				}
				while (field == null && !c.equals(Object.class)) {
					// while field not found and not class == object.class
					// search for field with name 'fieldName'
					if (c.getDeclaredFields() == null
							|| c.getDeclaredFields().length == 0) {
						// no fields declared, try superclass
						i = 0;
						c = c.getSuperclass();
					} else if (!c.getDeclaredFields()[i].getName().equals(
							fieldName)) {
						// actual field was not the one we're looking for
						if (i < c.getDeclaredFields().length - 1) {
							// try next if there is one
							i++;
						} else {
							// else try in superclass
							i = 0;
							c = c.getSuperclass();
						}
					} else {
						// field was found
						field = c.getDeclaredFields()[i];
						if (!fieldName.equals(property)) {
							// get the class of the property and search deeper
							i = 0;
							if (!field.getType().equals(List.class)) {
								c = field.getType();
							} else {
								// the field is a list field
								if (field.getGenericType() instanceof ParameterizedType) {
									// get the generic type of the list
									ParameterizedType paramType = (ParameterizedType) field
											.getGenericType();
									if (paramType != null
											&& paramType
													.getActualTypeArguments().length == 1
											&& paramType
													.getActualTypeArguments()[0] instanceof Class) {
										// new class to search for field named
										// 'fieldName' is the generic type of
										// the list
										c = (Class<?>) paramType
												.getActualTypeArguments()[0];
									}
								}
							}
						}
					}
				}
				if (!fieldName.equals(property)) {
					// if not set field to null, the next run of the first
					// while-loop would not start the inner second while-loop
					field = null;
				}
			}
		}
		return field;
	}

	private IModelComponent[] getDescriptors(Class<?> clazz, String query) {
		EntityManager em = newManager();
		List<IModelComponent> result = new ArrayList<>();
		try {
			List<?> list = em.createQuery(query).getResultList();
			for (Object r : list) {
				Object[] attributes = (Object[]) r;
				IModelComponent descriptor = (IModelComponent) clazz
						.newInstance();
				if (attributes[0] != null) {
					descriptor.setId(attributes[0].toString());
				}
				if (attributes[1] != null) {
					descriptor.setName(attributes[1].toString());
				}
				if (attributes[2] != null) {
					descriptor.setDescription(attributes[2].toString());
				}
				if (attributes[3] != null) {
					descriptor.setCategoryId(attributes[3].toString());
				}
				if (attributes.length > 4 && attributes[4] != null) {
					if (clazz == Flow.class) {
						((Flow) descriptor)
								.setFlowType((FlowType) attributes[4]);
					} else if (clazz == Process.class) {
						((Process) descriptor)
								.setProcessType((ProcessType) attributes[4]);
					}
				}
				if (attributes.length > 5 && attributes[5] instanceof Location) {
					Location loc = (Location) attributes[5];
					if (descriptor instanceof Flow)
						((Flow) descriptor).setLocation(loc);
					if (descriptor instanceof Process)
						((Process) descriptor).setLocation(loc);
				}
				result.add(descriptor);
			}
		} catch (Exception e) {
			log.error("Get descriptors failed", e);
		} finally {
			em.close();
		}

		return result.toArray(new IModelComponent[result.size()]);
	}

	/**
	 * Get the type of the first list field occuring in the property string
	 * 
	 * <br>
	 * <br>
	 * Examples: <br>
	 * 'singleField1.singleField2.listField' -> <b>returns</b> listField<br>
	 * 'singleField1.singleField2.listField1.singleField3.listField2' ->
	 * <b>returns</b> listField1<br>
	 * 'singleField1.singleField2' -> <b>returns</b> null <br>
	 * 'listField1.singleField1.singleField2 -> <b>returns</b> listField1<br>
	 * 
	 * @param clazz
	 *            the class to begin searching
	 * @param prop
	 *            The property name
	 * @return the class of the first list field in the given property
	 */
	private Class<?> getListFieldType(Class<?> clazz, String prop) {
		String property = prop;
		Class<?> listFieldType = null;
		if (property != null && clazz != null) {
			int i = 0;
			Class<?> c = clazz;
			String fieldName = "";
			while (listFieldType == null && !fieldName.equals(property)) {
				// if listFieldType != null -> return
				// if (fieldName.equals(property)) no list field exists in the
				// given property string
				fieldName = property;
				if (property.contains(".")) {
					fieldName = property.substring(0, property.indexOf('.'));
					property = property.substring(property.indexOf('.') + 1);
				}
				Field field = null;
				while (field == null && !c.equals(Object.class)) {
					if (c.getDeclaredFields() == null
							|| c.getDeclaredFields().length == 0) {
						// no fields declared, try superclass
						i = 0;
						c = c.getSuperclass();
					} else if (!c.getDeclaredFields()[i].getName().equals(
							fieldName)) {
						// actual field was not the one we're looking for
						if (i < c.getDeclaredFields().length - 1) {
							// try next if there is one
							i++;
						} else {
							// else try in superclass
							i = 0;
							c = c.getSuperclass();
						}
					} else {
						// field was found
						field = c.getDeclaredFields()[i];
						if (!field.getType().equals(List.class)) {
							// get the class of the property and search deeper
							i = 0;
							c = field.getType();
						} else {
							// the field is a list field
							if (field.getGenericType() instanceof ParameterizedType) {
								// get the generic type of the list
								ParameterizedType paramType = (ParameterizedType) field
										.getGenericType();
								if (paramType != null
										&& paramType.getActualTypeArguments().length == 1
										&& paramType.getActualTypeArguments()[0] instanceof Class) {
									listFieldType = (Class<?>) paramType
											.getActualTypeArguments()[0];
								}
							}
						}
					}
				}
			}
		}
		return listFieldType;
	}

	/**
	 * get the string until and including the first list field <br>
	 * <br>
	 * Example: <br>
	 * <b>property:</b> singleField1.singleField2.listField.singleField3 ->
	 * <b>returns</b> singleField1.singleField2.listField
	 * 
	 * @param clazz
	 *            the class of the object to search the field in
	 * @param prop
	 *            The property name
	 * @return substring of property until and including the first list field
	 */
	private String getPropertyUntilFirstListField(Class<?> clazz, String prop) {
		String property = prop;
		String propertyUntilfirstListField = "";
		boolean foundListField = false;
		if (property != null && clazz != null) {
			int i = 0;
			Class<?> c = clazz;
			String fieldName = "";
			while (!foundListField && !fieldName.equals(property)) {
				// if foundListField return
				// if (fieldName.equals(property)) no list field exists in the
				// given property string
				fieldName = property;
				if (property.contains(".")) {
					fieldName = property.substring(0, property.indexOf('.'));
					property = property.substring(property.indexOf('.') + 1);
				}
				Field field = null;
				while (field == null && !c.equals(Object.class)) {
					if (c.getDeclaredFields() == null
							|| c.getDeclaredFields().length == 0) {
						// no fields declared, try superclass
						i = 0;
						c = c.getSuperclass();
					} else if (!c.getDeclaredFields()[i].getName().equals(
							fieldName)) {
						// actual field was not the one we're looking for
						if (i < c.getDeclaredFields().length - 1) {
							// try next if there is one
							i++;
						} else {
							// else try in superclass
							i = 0;
							c = c.getSuperclass();
						}
					} else {
						// field was found
						field = c.getDeclaredFields()[i];
						propertyUntilfirstListField += field.getName() + ".";
						if (!field.getType().equals(List.class)) {
							// get the class of the property and search deeper
							i = 0;
							c = field.getType();
						} else {
							// the field is a list field
							if (field.getGenericType() instanceof ParameterizedType) {
								// get the generic type of the list
								ParameterizedType paramType = (ParameterizedType) field
										.getGenericType();
								if (paramType != null
										&& paramType.getActualTypeArguments().length == 1
										&& paramType.getActualTypeArguments()[0] instanceof Class) {
									foundListField = true;
								}
							}
						}
					}
				}
			}
		}
		if (!foundListField) {
			propertyUntilfirstListField = null;
		} else {
			if (propertyUntilfirstListField != null) {
				// remove the last '.';
				propertyUntilfirstListField = propertyUntilfirstListField
						.substring(0, propertyUntilfirstListField.length() - 1);
			}
		}
		return propertyUntilfirstListField;
	}

	/**
	 * Create a query from the given class and properties
	 * 
	 * @param clazz
	 *            the result class
	 * @param properties
	 *            the properties of the object0
	 * @param strict
	 *            if true only exact values are true, otherwise LIKE is used
	 * @param descriptor
	 *            if true only get id, name and description
	 * @param singleResult
	 *            if true add distinct after select
	 * @return a query as String
	 */
	private String getQuery(Class<?> clazz, Map<String, Object> properties,
			boolean strict, boolean descriptor, boolean singleResult) {
		String query = "SELECT ";
		if (singleResult) {
			query += "DISTINCT ";
		}
		if (descriptor) {
			query += "t0.id, t0.name, t0.description, t0.categoryId";
			if (clazz == Flow.class) {
				query += ", t0.flowType";
				query += ", t0.location";
			} else if (clazz == Process.class) {
				query += ", t0.processType";
				query += ", t0.location";
			}
		} else {
			query += "t0";
		}
		query += " FROM " + clazz.getSimpleName() + " t0";
		if (properties != null && !properties.isEmpty()) {
			query += getWhereClause(clazz, properties, strict);
		}
		return query;
	}

	/**
	 * Get all properties which refer to a field in the given class
	 * 
	 * @param clazz
	 *            - the class where to search the fieldNames
	 * @param properties
	 *            The properties
	 * @return a map of string and boolean. The string represents the valid
	 *         property and the boolean if the field is from type String or not
	 */
	private Map<String, Class<?>> getRelevantFieldNames(Class<?> clazz,
			Map<String, Object> properties) {
		Map<String, Class<?>> relevantFields = new HashMap<>();
		for (String propertyKey : properties.keySet()) {
			Field field = getDeclaredField(clazz, propertyKey);
			if (field != null) {
				relevantFields.put(propertyKey, field.getType());
			}
		}
		return relevantFields;
	}

	/**
	 * Creates the where clause for a SQL query with the given properties and
	 * result class
	 * 
	 * @param clazz
	 *            the result class
	 * @param properties
	 *            the properties
	 * @param strict
	 *            if true only exact matches are valid else LIKE is used for
	 *            comparison
	 * @return The where clause for the given properties as String
	 */
	private String getWhereClause(Class<?> clazz,
			Map<String, Object> properties, boolean strict) {
		// key = fieldName, value = field.getType().equals(String.class)
		String whereClause = "";
		Map<String, Class<?>> fieldNames = getRelevantFieldNames(clazz,
				properties);
		boolean first = true;
		int i = 1;
		for (Entry<String, Class<?>> entry : fieldNames.entrySet()) {
			if (!first) {
				whereClause += " AND ";
			} else {
				whereClause += " WHERE ";
			}
			whereClause += getCondition(clazz, entry.getKey(),
					properties.get(entry.getKey()), entry.getValue(), strict,
					i, 0);
			first = false;
			i += 10;
		}
		return whereClause;
	}

	/**
	 * Searches a category
	 * 
	 * @param categoryToSearchIn
	 *            The category to search in
	 * @param categoryIdToSearchFor
	 *            The category to search for
	 * @return The found category
	 */
	private Category searchForCategory(Category categoryToSearchIn,
			String categoryIdToSearchFor) {
		Category databaseCategory = null;
		int i = 0;
		while (databaseCategory == null
				&& i < categoryToSearchIn.getChildCategories().length) {
			if (categoryToSearchIn.getChildCategories()[i].getId().equals(
					categoryIdToSearchFor)) {
				databaseCategory = categoryToSearchIn.getChildCategories()[i];
			} else {
				databaseCategory = searchForCategory(
						categoryToSearchIn.getChildCategories()[i],
						categoryIdToSearchFor);
				i++;
			}
		}
		return databaseCategory;
	}

	@Override
	public void delete(Object object) throws DataProviderException {
		try {
			log.trace("Delete {}", object);
			// Entity (Object) has to be merged first. The returned merged
			// entity can be removed
			EntityManager entityManager = newManager();
			entityManager.getTransaction().begin();
			entityManager.remove(entityManager.merge(object));
			entityManager.getTransaction().commit();
			entityManager.close();
		} catch (Exception e) {
			throw new DataProviderException("Could not execute delete: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public void insert(Object object) throws DataProviderException {
		try {
			log.trace("Insert {}", object);
			EntityManager entityManager = newManager();
			entityManager.getTransaction().begin();
			entityManager.persist(object);
			entityManager.getTransaction().commit();
			entityManager.close();
		} catch (Exception e) {
			throw new DataProviderException("Could not execute insert: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public Object[] query(String query) {
		log.trace("Query {}", query);
		try {
			EntityManager em = newManager();
			List<?> list = em.createQuery(query).getResultList();
			Object[] result = null;
			if (list != null) {
				result = list.toArray(new Object[list.size()]);
			}
			em.close();
			return result;
		} catch (Exception e) {
			log.error("Could not execute query " + query, e);
			return new Object[0];
		}
	}

	@Override
	public <T> T select(Class<T> clazz, String id) throws DataProviderException {
		log.trace("Select {} with id {}", clazz, id);
		try {
			T o = null;
			if (clazz == Category.class) {
				o = clazz.cast(getCategory(id));
			} else if (clazz != null && id != null) {
				// get object for id
				EntityManager entityManager = newManager();
				o = entityManager.find(clazz, id);
				entityManager.close();
			}
			return o;
		} catch (Exception e) {
			throw new DataProviderException("Could not execute select: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public <T> List<T> selectAll(Class<T> clazz, Map<String, Object> properties)
			throws DataProviderException {
		log.trace("Select all for {} with {}", clazz, properties);
		try {
			String query = getQuery(clazz, properties, true, false, false);
			EntityManager em = newManager();
			List<T> result = em.createQuery(query, clazz).getResultList();
			em.close();
			return result;
		} catch (Exception e) {
			throw new DataProviderException("Could not execute select: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public IModelComponent selectDescriptor(Class<?> objectClass, String id)
			throws DataProviderException {
		log.trace("Select descriptor {} with id {}", objectClass, id);
		try {
			Map<String, Object> properties = new HashMap<>();
			properties.put("id", id);
			String query = getQuery(objectClass, properties, true, true, false);
			IModelComponent result = null;
			IModelComponent[] results = getDescriptors(objectClass, query);
			if (results.length > 0) {
				result = results[0];
			}
			return result;
		} catch (Exception e) {
			throw new DataProviderException("Could not execute select: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public IModelComponent[] selectDescriptors(Class<?> objectClass)
			throws DataProviderException {
		log.trace("Select descriptors {}", objectClass);
		try {
			return selectDescriptors(objectClass, new HashMap<String, Object>());
		} catch (Exception e) {
			throw new DataProviderException("Could not execute select: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public IModelComponent[] selectDescriptors(Class<?> objectClass,
			Map<String, Object> properties) throws DataProviderException {
		log.trace("Select descriptors {} with {}", objectClass, properties);
		try {
			String query = getQuery(objectClass, properties, true, true, false);
			return getDescriptors(objectClass, query);
		} catch (Exception e) {
			throw new DataProviderException("Could not execute select: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public void update(Object object) throws DataProviderException {
		try {
			log.trace("Update {}", object);
			EntityManager entityManager = newManager();
			EntityTransaction transaction = entityManager.getTransaction();
			transaction.begin();
			entityManager.merge(object);
			transaction.commit();
			entityManager.close();
		} catch (Exception e) {
			throw new DataProviderException("Could not execute update: "
					+ e.getMessage(), e);
		}
	}

	@Override
	public abstract EntityManagerFactory getEntityFactory();

	@Override
	public <T> BaseDao<T> createDao(Class<T> clazz) {
		return new BaseDao<>(clazz, getEntityFactory());
	}

	private EntityManager newManager() {
		return getEntityFactory().createEntityManager();
	}

}
