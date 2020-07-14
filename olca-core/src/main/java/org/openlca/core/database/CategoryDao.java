package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Categories;
import org.openlca.util.Strings;

public class CategoryDao
		extends CategorizedEntityDao<Category, CategoryDescriptor> {

	private static Map<ModelType, String> tables;

	public CategoryDao(IDatabase database) {
		super(Category.class, CategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] {
				"id",
				"ref_id",
				"name",
				"description",
				"version",
				"last_change",
				"f_category",
				"library",
				"model_type",
		};
	}

	@Override
	protected CategoryDescriptor createDescriptor(Object[] queryResult) {
		var descriptor = super.createDescriptor(queryResult);
		if (queryResult[8] instanceof String) {
			descriptor.categoryType = ModelType
					.valueOf((String) queryResult[8]);
		}
		return descriptor;
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories(ModelType type) {
		String jpql = "select c from Category c where c.category is null and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories() {
		String jpql = "select c from Category c where c.category is null";
		Map<String, Object> m = Collections.emptyMap();
		return getAll(jpql, m);
	}

	@Override
	// see update(category)
	public Category insert(Category category) {
		category.refId = Categories.createRefId(category);
		Category existing = getForRefId(category.refId);
		if (existing != null) {
			mergeChildren(existing, category);
			return update(existing);
		}
		return super.insert(category);
	}

	// categories should be identified by their path, therefore the refID will
	// be generated depending on the category path. This way, we can treat the
	// category model as a normal entity and still compare categories by path
	@Override
	public Category update(Category category) {
		String refId = category.refId;
		String newRefId = Categories.createRefId(category);
		Category forRefId = getForRefId(newRefId);
		boolean isNew = category.id == 0L;
		if (!Objects.equals(refId, newRefId) && !isNew)
			getDatabase().notifyDelete(Descriptor.of(category));
		if (Objects.equals(refId, newRefId) || forRefId == null) {
			category.refId = newRefId;
			category = super.update(category);
			for (Category child : category.childCategories)
				update(child);
			if (!Objects.equals(refId, newRefId) && !isNew) {
				updateModels(category);
			}
			return category;
		}
		mergeChildren(forRefId, category);
		forRefId = super.update(forRefId);
		for (Category child : forRefId.childCategories)
			update(child);
		if (!Objects.equals(refId, newRefId) && !isNew)
			updateModels(category);
		return forRefId;
	}

	private void mergeChildren(Category into, Category from) {
		for (Category child : from.childCategories) {
			if (contains(into.childCategories, child))
				continue;
			child.category = into;
			into.childCategories.add(child);
		}
	}

	private boolean contains(List<Category> categories, Category category) {
		for (Category child : categories)
			if (Categories.createRefId(child)
					.equals(Categories.createRefId(category)))
				return true;
		return false;
	}

	private <T extends CategorizedEntity> void updateModels(Category category) {
		Optional<Category> optional = Optional.ofNullable(category);
		for (CategorizedDescriptor descriptor : getDescriptors(
				category.modelType, optional)) {
			Version v = new Version(descriptor.version);
			v.incUpdate();
			long version = v.getValue();
			long lastChange = System.currentTimeMillis();
			descriptor.version = version;
			descriptor.lastChange = lastChange;
			String update = "UPDATE " + getTable(descriptor.type)
					+ " SET version = " + version + ", last_change = "
					+ lastChange + " WHERE id = " + descriptor.id;
			NativeSql.on(database).runUpdate(update);
			database.notifyUpdate(descriptor);
		}
	}

	private String getTable(ModelType modelType) {
		if (tables == null) {
			tables = new HashMap<>();
			for (ModelType type : ModelType.values()) {
				if (type.getModelClass() == null || !RootEntity.class
						.isAssignableFrom(type.getModelClass()))
					continue;
				String table = Daos.root(database, type).getEntityTable();
				tables.put(type, table);
			}
		}
		return tables.get(modelType);
	}

	private <T extends CategorizedEntity> List<? extends CategorizedDescriptor> getDescriptors(
			ModelType type,
			Optional<Category> category) {
		if (type == null || !type.isCategorized())
			return new ArrayList<>();
		return Daos.categorized(getDatabase(), type).getDescriptors(category);
	}

	/**
	 * Creates the categories for the segments of the given path that do not yet
	 * exist and returns the category of the last segment. If the given path is
	 * empty or null, null is returned.
	 */
	public Category sync(ModelType type, String... path) {
		if (path == null || path.length == 0)
			return null;
		Category parent = null;
		List<Category> next = getRootCategories(type);
		for (int i = 0; i < path.length; i++) {
			String segment = path[i];
			if (Strings.nullOrEmpty(segment))
				continue;
			segment = segment.trim();
			if (segment.isEmpty())
				continue;
			Category category = null;
			for (Category c : next) {
				if (segment.equalsIgnoreCase(c.name)) {
					category = c;
					break;
				}
			}
			if (category != null) {
				parent = category;
				next = category.childCategories;
				continue;
			}
			category = new Category();
			category.name = segment;
			category.lastChange = new Date().getTime();
			category.modelType = type;
			category.version = Version.valueOf(0, 0, 1);
			category.category = parent;
			category.refId = Categories.createRefId(category);
			if (parent == null) {
				category = insert(category);
			} else {
				parent.childCategories.add(category);
				parent = update(parent);
				// need to find the category now that is in sync with JPA
				for (Category child : parent.childCategories) {
					if (Objects.equals(child.refId, category.refId)) {
						category = child;
						break;
					}
				}
			}
			parent = category;
			next = category.childCategories;
		}
		return parent;
	}

}
