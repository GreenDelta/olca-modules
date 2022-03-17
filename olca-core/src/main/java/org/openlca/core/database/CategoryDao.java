package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Categories;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

public class CategoryDao
	extends RootEntityDao<Category, CategoryDescriptor> {

	private static Map<ModelType, String> tables;

	public CategoryDao(IDatabase database) {
		super(Category.class, CategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[]{
			"id",
			"ref_id",
			"name",
			"description",
			"version",
			"last_change",
			"f_category",
			"library",
			"tags",
			"model_type",
		};
	}

	@Override
	protected CategoryDescriptor createDescriptor(Object[] record) {
		if (record == null)
			return null;
		var d = super.createDescriptor(record);
		if (record[9] instanceof String) {
			d.categoryType = ModelType.valueOf((String) record[9]);
		}
		return d;
	}

	/**
	 * Root categories do not have a parent category.
	 */
	public List<Category> getRootCategories(ModelType type) {
		String jpql = "select c from Category c where c.category is null and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

	/**
	 * Root categories do not have a parent category.
	 */
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

	private void updateModels(Category category) {
		if (category == null)
			return;
		for (var d : getDescriptors(category.modelType, Optional.of(category))) {
			Version v = new Version(d.version);
			v.incUpdate();
			long version = v.getValue();
			long lastChange = System.currentTimeMillis();
			d.version = version;
			d.lastChange = lastChange;
			String update = "UPDATE " + getTable(d.type)
											+ " SET version = " + version + ", last_change = "
											+ lastChange + " WHERE id = " + d.id;
			NativeSql.on(db).runUpdate(update);
			db.notifyUpdate(d);
		}
	}

	private String getTable(ModelType modelType) {
		if (tables == null) {
			tables = new HashMap<>();
			for (ModelType type : ModelType.values()) {
				if (type.getModelClass() == null || !RefEntity.class
					.isAssignableFrom(type.getModelClass()))
					continue;
				String table = Daos.refDao(db, type).getEntityTable();
				tables.put(type, table);
			}
		}
		return tables.get(modelType);
	}

	private List<? extends RootDescriptor> getDescriptors(
		ModelType type,
		Optional<Category> category) {
		if (type == null || !type.isRoot())
			return new ArrayList<>();
		return Daos.root(getDatabase(), type).getDescriptors(category);
	}

	public static Category sync(IDatabase db, ModelType type, String... path) {
		return db == null
			? null
			: new CategoryDao(db).sync(type, path);
	}

	/**
	 * Creates the categories for the segments of the given path that do not yet
	 * exist and returns the category of the last segment. If the given path is
	 * empty or null, null is returned.
	 */
	public Category sync(ModelType type, String... path) {
		if (type == null || path == null || path.length == 0)
			return null;
		Category parent = null;
		List<Category> next = getRootCategories(type);
		for (var segment : path) {
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

	public Category getForPath(ModelType type, String path) {
		if (type == null || path == null)
			return null;

		// first try via the refId
		var refId = KeyGen.get(path);
		var withRefId = getForRefId(refId);
		if (withRefId != null)
			return withRefId;

		// traverse the tree
		var parts = path.split("/");
		var next = getRootCategories(type);
		Category category = null;
		for (var p : parts) {
			var part = p.trim();
			if (part.isEmpty())
				continue;
			category = null;
			for (var c : next) {
					if (c.name == null)
						continue;
					if (c.name.trim().equalsIgnoreCase(part)) {
						category = c;
						next = c.childCategories;
						break;
					}
			}
			if (category == null)
				return null;
		}
		return category;
	}

}
