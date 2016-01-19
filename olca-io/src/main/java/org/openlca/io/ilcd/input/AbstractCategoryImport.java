package org.openlca.io.ilcd.input;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

abstract class AbstractCategoryImport<C> {

	private CategoryDao dao;
	private ModelType modelType;

	public AbstractCategoryImport(ImportConfig config, ModelType modelType) {
		this.modelType = modelType;
		dao = new CategoryDao(config.db);
	}

	/** Hook method that needs to be implemented by the concrete sub-class */
	protected abstract String getName(C ilcdCategory);

	public Category run(List<C> input) throws ImportException {
		if (input == null || input.isEmpty())
			return null;
		try {
			Category category = findRoot(input.get(0));
			if (category != null) {
				// root exists
				category = importCategories(category, input);
			} else {
				// all new
				category = createNew(input.get(0));
				dao.insert(category);
				for (int i = 1; i < input.size(); i++)
					category = createAndSave(category, input.get(i));
			}
			return category;
		} catch (Exception e) {
			throw new ImportException("Failed to insert categories", e);
		}
	}

	private Category findRoot(C c) {
		List<Category> roots = dao.getRootCategories(modelType);
		if (roots == null || roots.isEmpty())
			return null;
		String cName = getName(c);
		for (Category root : roots) {
			if (StringUtils.equalsIgnoreCase(root.getName(), cName))
				return root;
		}
		return null;
	}

	private Category importCategories(Category rootCategory,
			List<C> ilcdCategories) throws ImportException {
		Category nextRootCategory = rootCategory;
		for (C ilcdCategory : ilcdCategories) {
			Category category = findCategory(nextRootCategory, ilcdCategory);
			if (category == null) {
				category = createAndSave(nextRootCategory, ilcdCategory);
			}
			nextRootCategory = category;
		}
		return nextRootCategory;
	}

	private Category findCategory(Category rootCategory, C ilcdCategory) {
		if (equals(rootCategory, ilcdCategory))
			return rootCategory;
		List<Category> categories = rootCategory.getChildCategories();
		Category equalCategory = null;
		for (Category category : categories) {
			if (equals(category, ilcdCategory)) {
				equalCategory = category;
				break;
			}
		}
		return equalCategory;
	}

	private boolean equals(Category category, C ilcdCategory) {
		String name = getName(ilcdCategory);
		return category.getName() != null
				&& category.getName().equalsIgnoreCase(name);
	}

	private Category createAndSave(Category parentCategory, C ilcdCategory)
			throws ImportException {
		try {
			Category newCategory = createNew(ilcdCategory);
			newCategory.setCategory(parentCategory);
			parentCategory.getChildCategories().add(newCategory);
			dao.insert(newCategory);
			dao.update(parentCategory);
			return newCategory;
		} catch (Exception e) {
			String message = "Cannot save category in database.";
			throw new ImportException(message, e);
		}
	}

	private Category createNew(C ilcdCategory) {
		Category category = new Category();
		category.setModelType(modelType);
		category.setRefId(UUID.randomUUID().toString());
		String name = getName(ilcdCategory);
		category.setName(name);
		return category;
	}
}
