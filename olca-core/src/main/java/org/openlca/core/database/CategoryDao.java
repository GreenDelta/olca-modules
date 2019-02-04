package org.openlca.core.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.util.Categories;

public class CategoryDao
		extends CategorizedEntityDao<Category, CategoryDescriptor> {

	private static Map<ModelType, String> tables;

	public CategoryDao(IDatabase database) {
		super(Category.class, CategoryDescriptor.class, database);
	}

	@Override
	protected String[] getDescriptorFields() {
		return new String[] { "id", "ref_id", "name", "description", "version",
				"last_change", "f_category", "model_type" };
	}

	@Override
	protected CategoryDescriptor createDescriptor(Object[] queryResult) {
		CategoryDescriptor descriptor = super.createDescriptor(queryResult);
		if (queryResult[7] instanceof String)
			descriptor.categoryType = ModelType.valueOf((String) queryResult[7]);
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

	@Override
	// categories should be identified by their path, therefore the refID will
	// be generated depending on the category path. This way, we can treat the
	// category model as a normal entity and still compare categories by path
	public Category update(Category category) {
		String refId = category.refId;
		String newRefId = Categories.createRefId(category);
		Category forRefId = getForRefId(newRefId);
		boolean isNew = category.id == 0l;
		if (!Objects.equals(refId, newRefId) && !isNew)
			getDatabase().notifyDelete(Descriptors.toDescriptor(category));
		if (Objects.equals(refId, newRefId) || forRefId == null) {
			category.refId = newRefId;
			category = super.update(category);
			for (Category child : category.getChildCategories())
				update(child);
			if (!Objects.equals(refId, newRefId) && !isNew) {
				updateModels(category);
			}
			return category;
		}
		mergeChildren(forRefId, category);
		forRefId = super.update(forRefId);
		for (Category child : forRefId.getChildCategories())
			update(child);
		if (!Objects.equals(refId, newRefId) && !isNew)
			updateModels(category);
		return forRefId;
	}

	private void mergeChildren(Category into, Category from) {
		for (Category child : from.getChildCategories()) {
			if (contains(into.getChildCategories(), child))
				continue;
			child.category = into;
			into.getChildCategories().add(child);
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
				category.getModelType(), optional)) {
			Version v = new Version(descriptor.version);
			v.incUpdate();
			long version = v.getValue();
			long lastChange = System.currentTimeMillis();
			descriptor.version = version;
			descriptor.lastChange = lastChange;
			try {
				String update = "UPDATE " + getTable(descriptor.type)
						+ " SET version = " + version + ", last_change = "
						+ lastChange
						+ " WHERE id = " + descriptor.id;
				NativeSql.on(database).runUpdate(update);
			} catch (SQLException e) {
				log.error("Error updating "
						+ descriptor.type.getModelClass()
								.getSimpleName()
						+ " "
						+ descriptor.id, e);
			}
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

}
