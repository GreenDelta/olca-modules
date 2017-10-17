package org.openlca.io;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for the import, export, and display of categories.
 */
public final class Categories {

	public static Category findOrAddChild(IDatabase database, Category parent,
			String childName) {
		for (Category child : parent.getChildCategories()) {
			if (StringUtils.equalsIgnoreCase(child.getName(), childName))
				return child;
		}
		try {
			Category child = new Category();
			child.setModelType(parent.getModelType());
			child.setName(childName);
			child.setRefId(UUID.randomUUID().toString());
			child.setCategory(parent);
			parent.getChildCategories().add(child);
			CategoryDao dao = new CategoryDao(database);
			dao.insert(child);
			new CategoryDao(database).update(parent);
			return child;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Categories.class);
			log.error("failed to add child", e);
			return null;
		}
	}

	public static Category findOrCreateRoot(IDatabase database, ModelType type,
			String name) {
		Category root = findRoot(database, type, name);
		if (root != null)
			return root;
		else
			return createRoot(database, type, name);
	}

	/**
	 * Creates a new root category with the given name and of the given type and
	 * inserts it into the given database.
	 */
	public static Category createRoot(IDatabase database, ModelType type,
			String name) {
		try {
			Category category = new Category();
			category.setRefId(UUID.randomUUID().toString());
			category.setModelType(type);
			category.setName(name);
			new CategoryDao(database).insert(category);
			return category;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Categories.class);
			log.error("failed to insert root category " + name + " / " + type,
					e);
			return null;
		}
	}

	/**
	 * Returns a root category with the given name and type from the database or
	 * null if no such category exists. The case of the names are ignored.
	 */
	public static Category findRoot(IDatabase database, ModelType type,
			String name) {
		if (database == null || type == null || name == null)
			return null;
		try {
			CategoryDao dao = new CategoryDao(database);
			for (Category root : dao.getRootCategories(type)) {
				if (StringUtils.equalsIgnoreCase(root.getName(), name))
					return root;
			}
			return null;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Categories.class);
			log.error("failed to search root category " + name + " " + type, e);
			return null;
		}
	}

	public static Category findOrAdd(IDatabase database, ModelType type,
			String[] categories) {
		if (categories == null || categories.length == 0)
			return null;
		Category category = findOrCreateRoot(database, type, categories[0]);
		for (int i = 1; i < categories.length; i++)
			category = findOrAddChild(database, category, categories[i]);
		return category;
	}
}
