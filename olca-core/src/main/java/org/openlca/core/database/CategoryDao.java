package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoryDao extends BaseDao<Category> {

	public CategoryDao(EntityManagerFactory entityFactory) {
		super(Category.class, entityFactory);
	}

	/** Root categories do not have a parent category. */
	public List<Category> getRootCategories(ModelType type) throws Exception {
		String jpql = "select c from Category c where c.parentCategory is null "
				+ "and c.modelType = :type";
		return getAll(jpql, Collections.singletonMap("type", type));
	}

	/** Contains two categories at maximum: parent-category/category. */
	public String getShortPath(String categoryId) {
		if (categoryId == null)
			return "";
		try {
			Category category = getForId(categoryId);
			if (category == null || isRoot(category))
				return "";
			String path = "";
			Category parent = category.getParentCategory();
			if (isNotRoot(category))
				path = path.concat(parent.getName()).concat(" / ");
			return path.concat(category.getName());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to get category path for " + categoryId, e);
			return "";
		}
	}

	private boolean isRoot(Category category) {
		return !isNotRoot(category);
	}

	private boolean isNotRoot(Category category) {
		return category != null && category.getParentCategory() != null
				&& category.getParentCategory().getParentCategory() != null;
	}

	public List<? extends BaseDescriptor> getModelDescriptors(Category category)
			throws Exception {
		if (category == null || category.getModelType() == null)
			return Collections.emptyList();
		switch (category.getModelType()) {
		case ACTOR:
			return new ActorDao(getEntityFactory()).getDescriptors(category);
		case FLOW:
			return new FlowDao(getEntityFactory()).getDescriptors(category);
		case FLOW_PROPERTY:
			return new FlowPropertyDao(getEntityFactory())
					.getDescriptors(category);
		case IMPACT_METHOD:
			return new MethodDao(getEntityFactory()).getDescriptors(category);
		case PROCESS:
			return new ProcessDao(getEntityFactory()).getDescriptors(category);
		case PRODUCT_SYSTEM:
			return new ProductSystemDao(getEntityFactory())
					.getDescriptors(category);
		case PROJECT:
			return new ProjectDao(getEntityFactory()).getDescriptors(category);
		case SOURCE:
			return new SourceDao(getEntityFactory()).getDescriptors(category);
		case UNIT_GROUP:
			return new UnitGroupDao(getEntityFactory())
					.getDescriptors(category);
		default:
			log.warn("unknown category type {}", category.getModelType());
			return Collections.emptyList();
		}
	}

}
